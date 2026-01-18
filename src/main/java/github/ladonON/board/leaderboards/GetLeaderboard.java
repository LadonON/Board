package github.ladonON.board.leaderboards;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;

public class GetLeaderboard {
    private final JavaPlugin plugin;

    public GetLeaderboard(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static class BoardData {
        public String title;
        public String placeholder;
        public String prefix;
        
        // Text Formatting
        public String titleFormat;
        public String footerFormat;
        public String[] placeFormats;
        public String defaultPlaceFormat;
        
        // Visual Settings
        public String leaderboardType;
        public String rotationMode;
        public String headMode;
        public String sortOrder;
        
        // Layout Settings
        public double lineHeight;
        public float backgroundWidth;
        public boolean background;
        public int backgroundColor;
        
        // Display Settings
        public int maxDisplayEntries;
        public boolean showEmptyPlaces;
        public boolean decimalNumbers;
        public String numberLocale;

        public BoardData(String title, String placeholder, String prefix, 
                        String titleFormat, String footerFormat, String[] placeFormats, String defaultPlaceFormat,
                        String leaderboardType, String rotationMode, String headMode, String sortOrder,
                        double lineHeight, float backgroundWidth, boolean background, int backgroundColor,
                        int maxDisplayEntries, boolean showEmptyPlaces, boolean decimalNumbers, String numberLocale) {
            this.title = title;
            this.placeholder = placeholder;
            this.prefix = prefix;
            this.titleFormat = titleFormat;
            this.footerFormat = footerFormat;
            this.placeFormats = placeFormats;
            this.defaultPlaceFormat = defaultPlaceFormat;
            this.leaderboardType = leaderboardType;
            this.rotationMode = rotationMode;
            this.headMode = headMode;
            this.sortOrder = sortOrder;
            this.lineHeight = lineHeight;
            this.backgroundWidth = backgroundWidth;
            this.background = background;
            this.backgroundColor = backgroundColor;
            this.maxDisplayEntries = maxDisplayEntries;
            this.showEmptyPlaces = showEmptyPlaces;
            this.decimalNumbers = decimalNumbers;
            this.numberLocale = numberLocale;
        }
    }

    public Map<String, BoardData> getAllLeaderboards() {
        Map<String, BoardData> leaderboards = new HashMap<>();

        ConfigurationSection lbSection = plugin.getConfig().getConfigurationSection("leaderboards");

        if (lbSection == null) {
            plugin.getLogger().warning("No 'leaderboards' section found in config!");
            return leaderboards;
        }

        for (String key : lbSection.getKeys(false)) {
            ConfigurationSection boardSection = lbSection.getConfigurationSection(key);

            if (boardSection != null) {
                String title = boardSection.getString("title");
                String placeholder = boardSection.getString("placeholder");
                String prefix = boardSection.getString("prefix", "");
                
                // Text Formatting with defaults
                String titleFormat = boardSection.getString("titleFormat", "<gradient:#ff6000:#ffc663>--------- {title} ---------</gradient>");
                String footerFormat = boardSection.getString("footerFormat", "<gradient:#ffc663:#ff6000>----------------------------</gradient>");
                String[] placeFormats = boardSection.getStringList("placeFormats").toArray(new String[0]);
                if (placeFormats.length == 0) {
                    placeFormats = new String[] {
                        "<color:#fdcc00><bold>1.</bold></color> {head} <color:#fdcc00>{name}</color> {extra} <gray>{score}</gray> <white>{suffix}</white>",
                        "<color:#dcdcdc><bold>2.</bold></color> {head} <color:#dcdcdc>{name}</color> {extra} <gray>{score}</gray> <white>{suffix}</white>",
                        "<color:#e65f2f><bold>3.</bold></color> {head} <color:#e65f2f>{name}</color> {extra} <gray>{score}</gray> <white>{suffix}</white>"
                    };
                }
                String defaultPlaceFormat = boardSection.getString("defaultPlaceFormat", "<color:#ffb486><bold>{place}.</bold></color> {head} <color:#ffb486>{name}</color> {extra} <gray>{score}</gray> <white>{suffix}</white>");
                
                // Visual Settings with defaults
                String leaderboardType = boardSection.getString("leaderboardType", "TOP_PLAYER_HEAD");
                String rotationMode = boardSection.getString("rotationMode", "DYNAMIC");
                String headMode = boardSection.getString("headMode", "ITEM_DISPLAY");
                String sortOrder = boardSection.getString("sortOrder", "DESCENDING");
                
                // Layout Settings with defaults
                double lineHeight = boardSection.getDouble("lineHeight", 0.25);
                float backgroundWidth = (float) boardSection.getDouble("backgroundWidth", 40.0);
                boolean background = boardSection.getBoolean("background", true);
                int backgroundColor = boardSection.getInt("backgroundColor", 0x54000000);
                
                // Display Settings with defaults
                int maxDisplayEntries = boardSection.getInt("maxDisplayEntries", 10);
                boolean showEmptyPlaces = boardSection.getBoolean("showEmptyPlaces", true);
                boolean decimalNumbers = boardSection.getBoolean("decimalNumbers", false);
                String numberLocale = boardSection.getString("numberLocale", "GERMANY");

                leaderboards.put(key, new BoardData(title, placeholder, prefix, 
                        titleFormat, footerFormat, placeFormats, defaultPlaceFormat,
                        leaderboardType, rotationMode, headMode, sortOrder,
                        lineHeight, backgroundWidth, background, backgroundColor,
                        maxDisplayEntries, showEmptyPlaces, decimalNumbers, numberLocale));
            }
        }

        plugin.getLogger().info("Loaded " + leaderboards.size() + " leaderboards");
        return leaderboards;
    }
}