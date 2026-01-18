package github.ladonON.board.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Resolve {
    private final JavaPlugin plugin;
    
    public Resolve() {
        this.plugin = null;
    }
    
    public Resolve(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public int resolvePlaceholder(Player player, String placeholder) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            if (plugin != null) {
                plugin.getLogger().warning("PlaceholderAPI is not installed! Cannot resolve placeholder: " + placeholder);
            }
            return 0;
        }
        
        try {
            String raw = PlaceholderAPI.setPlaceholders(player, placeholder);
            if (plugin != null) {
                plugin.getLogger().info("Resolved placeholder '" + placeholder + "' to: '" + raw + "' for player " + player.getName());
            }
            
            raw = ChatColor.stripColor(raw);
            raw = raw.replaceAll("[^0-9.-]", "");
            
            if (raw.isEmpty()) {
                if (plugin != null) {
                    plugin.getLogger().warning("Placeholder '" + placeholder + "' resolved to empty string after filtering");
                }
                return 0;
            }
            
            try {
                double value = Double.parseDouble(raw);
                return (int) value;
            } catch (NumberFormatException e) {
                if (plugin != null) {
                    plugin.getLogger().warning("Could not parse number from placeholder '" + placeholder + "': " + raw);
                }
                return 0;
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().severe("Error resolving placeholder '" + placeholder + "': " + e.getMessage());
            }
            return 0;
        }
    }
}
