package github.ladonON.board.leaderboards;

import com.maximde.hologramlib.HologramLib;
import com.maximde.hologramlib.hologram.custom.LeaderboardHologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import github.ladonON.board.placeholders.Resolve;
import github.ladonON.board.data.SQLiteEngine;

public class Updater {

    private final JavaPlugin plugin;
    private final SQLiteEngine database;
    private final GetLeaderboard loader;
    private final Resolve resolver;
    private Map<String, GetLeaderboard.BoardData> allBoards;
    private final List<Consumer<Player>> updaters = new ArrayList<>();
    private final Map<String, LeaderboardHologram> liveLeaderboards = new HashMap<>();
    private BukkitTask updateTask;

    public Updater(JavaPlugin plugin) {
        this.plugin = plugin;
        this.resolver = new Resolve(plugin);
        this.loader = new GetLeaderboard(plugin);
        this.database = new SQLiteEngine(plugin);
        reloadData();
        respawnSavedLeaderboards();
    }

    public void reloadData() {
        this.allBoards = loader.getAllLeaderboards();
        this.updaters.clear();

        for (Map.Entry<String, GetLeaderboard.BoardData> entry : allBoards.entrySet()) {
            String boardID = entry.getKey();
            GetLeaderboard.BoardData data = entry.getValue();
            String tableName = "leaderboard_" + boardID.toLowerCase();
            database.createTable(tableName);

            plugin.getLogger().info("Created or verified table: " + tableName);

            final String placeholder = data.placeholder;
            final String title = data.title;

            updaters.add(player -> {
                int score = resolver.resolvePlaceholder(player, placeholder);
                database.saveScore(tableName, player.getUniqueId(), player.getName(), score);
                plugin.getLogger().info("Updated " + title + " for " + player.getName() + ": " + score);
            });
        }
    }

    public Map<String, Integer> getPlayerScores(Player player) {
        Map<String, Integer> playerScores = new HashMap<>();
        if (allBoards == null || allBoards.isEmpty()) {
            reloadData();
            if (allBoards.isEmpty()) return playerScores;
        }
        for (Map.Entry<String, GetLeaderboard.BoardData> entry : allBoards.entrySet()) {
            String boardID = entry.getKey();
            GetLeaderboard.BoardData data = entry.getValue();
            int score = resolver.resolvePlaceholder(player, data.placeholder);
            playerScores.put(boardID, score);
        }
        return playerScores;
    }

    public List<Consumer<Player>> getUpdaters() {
        return updaters;
    }

    public void runAll() {
        plugin.getLogger().info("Running updater for " + Bukkit.getOnlinePlayers().size() + " online players");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getLogger().info("Updating scores for player: " + player.getName());
            for (Consumer<Player> updater : updaters) {
                updater.accept(player);
            }
        }

        for (Map.Entry<String, LeaderboardHologram> entry : liveLeaderboards.entrySet()) {
            String tableName = "leaderboard_" + entry.getKey();
            LeaderboardHologram holo = entry.getValue();
            Map<UUID, Integer> scores = database.getAllScores(tableName);
            Map<UUID, String> names = database.getAllNames(tableName);
            
            plugin.getLogger().info("Updating hologram '" + entry.getKey() + "' with " + scores.size() + " scores");

            scores.forEach((uuid, score) -> {
                String name = names.getOrDefault(uuid, "Unknown");
                holo.setPlayerScore(uuid, name, (double) score);
            });
            holo.update();
        }
    }

    public void start(int intervalTicks) {
        if (updateTask != null) {
            updateTask.cancel();
        }
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                runAll();
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        despawnAllHolograms();
    }

    public void despawnAllHolograms() {
        for (LeaderboardHologram hologram : liveLeaderboards.values()) {
            HologramLib.getManager().get().remove(hologram);
        }
        liveLeaderboards.clear();
    }

    public SQLiteEngine getDatabase() {
        return database;
    }

    public Map<String, GetLeaderboard.BoardData> getAllBoards() {
        return allBoards;
    }

    public Map<String, LeaderboardHologram> getLiveLeaderboards() {
        return liveLeaderboards;
    }

    public void respawnSavedLeaderboards() {
        plugin.getLogger().info("Respawning saved leaderboards...");
        
        // Create the locations table first
        database.createLeaderboardLocationsTable();
        
        // Load all saved leaderboard locations
        Map<String, Location> savedLocations = database.loadAllLeaderboardLocations();
        
        for (Map.Entry<String, Location> entry : savedLocations.entrySet()) {
            String leaderboardId = entry.getKey();
            Location location = entry.getValue();
            
            // Check if this leaderboard exists in config
            if (allBoards.containsKey(leaderboardId)) {
                GetLeaderboard.BoardData data = allBoards.get(leaderboardId);
                String tableName = "leaderboard_" + leaderboardId.toLowerCase();
                
                plugin.getLogger().info("Respawning leaderboard '" + leaderboardId + "' at " + location.toString());
                
                // Create leaderboard options with all config settings
                LeaderboardHologram.LeaderboardOptions options = LeaderboardHologram.LeaderboardOptions.builder()
                        .title((data.prefix.isEmpty() ? data.title : data.prefix + " - " + data.title))
                        .titleFormat(data.titleFormat)
                        .footerFormat(data.footerFormat)
                        .placeFormats(data.placeFormats)
                        .defaultPlaceFormat(data.defaultPlaceFormat)
                        .suffix(data.title.toLowerCase())
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
                
                // Create and spawn leaderboard
                LeaderboardHologram leaderboard = new LeaderboardHologram(options, tableName);
                
                // Load existing scores
                database.getAllScores(tableName).forEach((uuid, score) -> {
                    String name = database.getAllNames(tableName).getOrDefault(uuid, "Unknown");
                    leaderboard.setPlayerScore(uuid, name, (double) score);
                });
                
                // Spawn at saved location
                HologramLib.getManager().get().spawn(leaderboard, location);
                leaderboard.update();
                
                // Add to live leaderboards
                liveLeaderboards.put(leaderboardId, leaderboard);
                
                plugin.getLogger().info("Successfully respawned leaderboard '" + leaderboardId + "'");
            } else {
                plugin.getLogger().warning("Saved leaderboard '" + leaderboardId + "' not, skipping...");
            }
        }
        
        plugin.getLogger().info("Respawned " + savedLocations.size() + " leaderboards");
    }
}
