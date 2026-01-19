package github.ladonON.board.commands;

import com.maximde.hologramlib.HologramLib;
import com.maximde.hologramlib.hologram.custom.LeaderboardHologram;
import github.ladonON.board.leaderboards.GetLeaderboard;
import github.ladonON.board.leaderboards.Updater;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BoardMain implements CommandExecutor {

    private final JavaPlugin plugin;

    public BoardMain(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("board.use")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /board <subcommand>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                subcommand_help(sender);
                return true;

            case "reload":
                subcommand_reload(sender);
                return true;

            case "spawn":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /board spawn <leaderboard>");
                    return true;
                }
                spawnLeaderboard(sender, args[1].toLowerCase());
                return true;

            case "update":
                subcommand_update(sender);
                return true;

            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /board remove <leaderboard>");
                    return true;
                }
                subcommand_remove(sender, args[1]);
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                return true;
        }
    }

    private void subcommand_help(CommandSender sender) {
        String helpURL = plugin.getConfig().getString("help-url", "https://github.com/LadonON");
        String helpHoverMsg = plugin.getConfig().getString("help-hover-msg", "Click to open help page");
        String helpMsg = plugin.getConfig().getString("help-msg", "See the help page: ");

        if (sender instanceof Player player) {
            TextComponent message = new TextComponent(helpMsg);
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + helpHoverMsg)));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, helpURL));
            player.spigot().sendMessage(message);
        } else {
            sender.sendMessage(helpMsg + " " + helpURL);
        }
    }

    private void subcommand_reload(CommandSender sender) {
        if (!sender.hasPermission("board.admin")) {
            sender.sendMessage(ChatColor.RED + "No Permission!");
            return;
        }

        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Board config reloaded!");

        if (plugin instanceof github.ladonON.board.Board mainPlugin) {
            Updater updater = mainPlugin.getUpdater();
            if (updater != null) {
                updater.reloadData();
                sender.sendMessage(ChatColor.GREEN + "Leaderboards reloaded!");
            }
        }
    }

    private void subcommand_update(CommandSender sender) {
        if (!sender.hasPermission("board.admin")) {
            sender.sendMessage(ChatColor.RED + "No Permission!");
            return;
        }

        if (plugin instanceof github.ladonON.board.Board mainPlugin) {
            Updater updater = mainPlugin.getUpdater();
            if (updater != null) {
                updater.runAll();
                sender.sendMessage(ChatColor.GREEN + "Leaderboards updated manually!");
            } else {
                sender.sendMessage(ChatColor.RED + "UP not available!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Plugin instance error.");
        }
    }

    private void subcommand_remove(CommandSender sender, String id) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can remove leaderboards!");
            return;
        }

        if (!(plugin instanceof github.ladonON.board.Board mainPlugin)) {
            sender.sendMessage(ChatColor.RED + "Plugin instance error.");
            return;
        }

        Updater updater = mainPlugin.getUpdater();
        if (updater != null) {
            // Remove from live leaderboards
            Map<String, LeaderboardHologram> liveLeaderboards = updater.getLiveLeaderboards();
            if (liveLeaderboards.containsKey(id)) {
                LeaderboardHologram hologram = liveLeaderboards.get(id);
                HologramLib.getManager().get().remove(hologram);
                liveLeaderboards.remove(id);
                sender.sendMessage(ChatColor.GREEN + "Leaderboard '" + id + "' removed");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Leaderboard '" + id + "' was not currently spawned.");
            }
            
            // Remove from database locations table
            updater.getDatabase().removeLeaderboardLocation(id);
            sender.sendMessage(ChatColor.GREEN + "Leaderboard '" + id + "' and its location data removed");
        } else {
            sender.sendMessage(ChatColor.RED + "Updater not available!");
        }
    }

    private void spawnLeaderboard(CommandSender sender, String id) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can spawn leaderboards!");
            return;
        }

        if (!(plugin instanceof github.ladonON.board.Board mainPlugin)) {
            sender.sendMessage(ChatColor.RED + "Plugin instance error.");
            return;
        }

        Updater updater = mainPlugin.getUpdater();
        Map<String, GetLeaderboard.BoardData> boards = updater.getAllBoards();

        if (!boards.containsKey(id)) {
            sender.sendMessage(ChatColor.RED + "Leaderboard '" + id + "' not found");
            return;
        }

        GetLeaderboard.BoardData data = boards.get(id);
        String tableName = "leaderboard_" + id;

        updater.getDatabase().createTable(tableName);

        // Create leaderboard options with all config settings
        LeaderboardHologram.LeaderboardOptions options = LeaderboardHologram.LeaderboardOptions.builder()
                .title((data.prefix.isEmpty() ? data.title : data.prefix + " - " + data.title))
                .titleFormat(data.titleFormat)
                .footerFormat(data.footerFormat)
                .placeFormats(data.placeFormats)
                .defaultPlaceFormat(data.defaultPlaceFormat)
                .suffix(data.title.toLowerCase() + "s")
                .maxDisplayEntries(data.maxDisplayEntries)
                .showEmptyPlaces(data.showEmptyPlaces)
                .leaderboardType(LeaderboardHologram.LeaderboardType.SIMPLE_TEXT) // Temporarily forced to avoid NPE
                .rotationMode(LeaderboardHologram.RotationMode.valueOf(data.rotationMode))
                .headMode(LeaderboardHologram.HeadMode.valueOf(data.headMode))
                .sortOrder(LeaderboardHologram.SortOrder.valueOf(data.sortOrder))
                .lineHeight(data.lineHeight)
                .backgroundWidth(data.backgroundWidth)
                .background(data.background)
                .backgroundColor(data.backgroundColor)
                .decimalNumbers(data.decimalNumbers)
                .numberLocale(java.util.Locale.forLanguageTag(data.numberLocale.toLowerCase()))
                .build();

        LeaderboardHologram leaderboard = new LeaderboardHologram(options, tableName);

        updater.getDatabase().getAllScores(tableName).forEach((uuid, score) -> {
            String name = updater.getDatabase().getAllNames(tableName).getOrDefault(uuid, "Unknown");
            leaderboard.setPlayerScore(uuid, name, (double) score);
        });

        HologramLib.getManager().get().spawn(leaderboard, player.getLocation());
        
        leaderboard.update();

        updater.getDatabase().saveLeaderboardLocation(id, player.getLocation());

        updater.getLiveLeaderboards().put(id, leaderboard);

        sender.sendMessage(ChatColor.GREEN + "Leaderboard '" + data.title + "' spawned!");
    }
}
