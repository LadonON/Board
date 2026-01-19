package github.ladonON.board.listeners;

import com.maximde.hologramlib.HologramLib;
import com.maximde.hologramlib.hologram.custom.LeaderboardHologram;
import github.ladonON.board.Board;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

public class HologramInteractListener implements Listener {
    
    private final Board plugin;
    
    public HologramInteractListener(Board plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        
        // Check if player is punching (left-click) with empty hand or any item
        if (event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            
            if (!player.hasPermission("board.admin")) {
                return;
            }
            
            // Check if there's a hologram at this location
            if (plugin.getUpdater() != null) {
                Map<String, LeaderboardHologram> liveLeaderboards = plugin.getUpdater().getLiveLeaderboards();
                
                for (Map.Entry<String, LeaderboardHologram> entry : liveLeaderboards.entrySet()) {
                    String leaderboardId = entry.getKey();
                    LeaderboardHologram hologram = entry.getValue();
                    
                    // Check if the clicked block is near the hologram location
                    if (isNearHologram(event.getClickedBlock().getLocation(), hologram.getLocation(), 2.0)) {
                        event.setCancelled(true);
                        removeLeaderboard(player, leaderboardId, hologram);
                        return;
                    }
                }
            }
        }
    }
    
    private boolean isNearHologram(org.bukkit.Location blockLoc, org.bukkit.Location holoLoc, double radius) {
        if (blockLoc.getWorld() != holoLoc.getWorld()) {
            return false;
        }
        
        return blockLoc.distance(holoLoc) <= radius;
    }
    
    private void removeLeaderboard(Player player, String leaderboardId, LeaderboardHologram hologram) {
        // Remove the hologram
        if (plugin.getUpdater() != null) {
            Map<String, LeaderboardHologram> liveLeaderboards = plugin.getUpdater().getLiveLeaderboards();
            
            // Remove the hologram
            HologramLib.getManager().get().remove(hologram);
            liveLeaderboards.remove(leaderboardId);
            
            // Remove from database locations table
            plugin.getUpdater().getDatabase().removeLeaderboardLocation(leaderboardId);
            
            // Remove entire leaderboard table with all scores
            String tableName = "leaderboard_" + leaderboardId;
            plugin.getUpdater().getDatabase().removeLeaderboardTable(tableName);
            
            player.sendMessage(ChatColor.GREEN + "Leaderboard '" + leaderboardId + "' has been completely removed (hologram, location, and all scores)");
        }
    }
}
