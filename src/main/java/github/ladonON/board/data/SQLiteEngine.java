package github.ladonON.board.data;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class SQLiteEngine {
    private final File dbFile;
    private final Logger logger;

    private Connection connection;

    public SQLiteEngine(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        File datafolder = plugin.getDataFolder();
        if (!datafolder.exists()) datafolder.mkdirs();
        dbFile = new File(datafolder, "player_data.db");
        logger.info("SQLiteEngine initialized. Database file: " + dbFile.getAbsolutePath());
        connect();
    }

    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (SQLException e) {
            logger.severe("Could not connect to database: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.severe("Could not disconnect from database: " + e.getMessage());
        }
    }

    public void createTable(String tablename) {
        logger.info("Attempting to create or verify table: " + tablename);
        String sql = """
        CREATE TABLE IF NOT EXISTS %s (
            uuid TEXT PRIMARY KEY,
            player_name TEXT NOT NULL,
            score INTEGER NOT NULL DEFAULT 0
        );
        """.formatted(tablename);

        try (Statement stmt = connection.createStatement()) {

            stmt.execute(sql);
            logger.info("Table '%s' created or verified successfully.".formatted(tablename));

            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet columns = metaData.getColumns(null, null, tablename, "player_name")) {
                if (!columns.next()) {
                    String addColumnSql = "ALTER TABLE %s ADD COLUMN player_name TEXT NOT NULL DEFAULT 'Unknown';".formatted(tablename);
                    stmt.execute(addColumnSql);
                    logger.info("Added 'player_name' column to table: " + tablename);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error creating or verifying table '%s': %s".formatted(tablename, e.getMessage()));
            // e.printStackTrace(); // Replaced with logger.severe
        }
    }

    public void createLeaderboardLocationsTable() {
        logger.info("Attempting to create or verify leaderboard locations table");
        String sql = """
        CREATE TABLE IF NOT EXISTS leaderboard_locations (
            leaderboard_id TEXT PRIMARY KEY,
            world TEXT NOT NULL,
            x DOUBLE NOT NULL,
            y DOUBLE NOT NULL,
            z DOUBLE NOT NULL,
            yaw FLOAT NOT NULL DEFAULT 0,
            pitch FLOAT NOT NULL DEFAULT 0
        );
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Leaderboard locations table created or verified successfully.");
        } catch (SQLException e) {
            logger.severe("Error creating leaderboard locations table: " + e.getMessage());
        }
    }

    public int getScore(String tablename, String uuid) {
        String sql = "SELECT score FROM %s WHERE uuid = ?".formatted(tablename);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("score");
            }
        } catch (SQLException e) {
            logger.severe("Error getting score from table '%s' for UUID '%s': %s".formatted(tablename, uuid, e.getMessage()));
        }
        return 0;
    }

    public Map<UUID, Integer> getAllScores(String tablename) {
        Map<UUID, Integer> allScores = new HashMap<>();
        String sql = "SELECT uuid, score FROM %s".formatted(tablename);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    allScores.put(UUID.fromString(rs.getString("uuid")), rs.getInt("score"));
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid UUID found in database: " + rs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting all scores from table '%s': %s".formatted(tablename, e.getMessage()));
        }
        return allScores;
    }

    public void saveScore(String tablename, UUID uuid, String playername, int score) {
        logger.info("Saving score for player '%s' (%s) to table '%s': %d".formatted(playername, uuid.toString(), tablename, score));
        String sql = """
                INSERT INTO %s (uuid, player_name, score)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    player_name = excluded.player_name,
                    score = excluded.score;
                """.formatted(tablename);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, playername);
            pstmt.setInt(3, score);

            pstmt.executeUpdate();
            logger.info("Score saved successfully for player '%s' to table '%s'.".formatted(playername, tablename));
        } catch (SQLException e) {
            logger.severe("Could not save score to table '%s' for player '%s' (%s): %s".formatted(tablename, playername, uuid.toString(), e.getMessage()));

        }
    }
    public Map<UUID, String> getAllNames(String tablename) {
        Map<UUID, String> names = new HashMap<>();

        String sql = "SELECT uuid, player_name FROM %s".formatted(tablename);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    names.put(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("player_name")
                    );
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid UUID found in database: " + rs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting all names from table '%s': %s".formatted(tablename, e.getMessage()));
            // e.printStackTrace(); // Replaced with logger.severe
        }

        return names;
    }

    public void saveLeaderboardLocation(String leaderboardId, Location location) {
        logger.info("Saving location for leaderboard '" + leaderboardId + "' at " + location.toString());
        String sql = """
                INSERT OR REPLACE INTO leaderboard_locations 
                (leaderboard_id, world, x, y, z, yaw, pitch)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, leaderboardId);
            pstmt.setString(2, location.getWorld().getName());
            pstmt.setDouble(3, location.getX());
            pstmt.setDouble(4, location.getY());
            pstmt.setDouble(5, location.getZ());
            pstmt.setFloat(6, location.getYaw());
            pstmt.setFloat(7, location.getPitch());
            
            pstmt.executeUpdate();
            logger.info("Leaderboard location saved successfully for '" + leaderboardId + "'");
        } catch (SQLException e) {
            logger.severe("Error saving leaderboard location for '" + leaderboardId + "': " + e.getMessage());
        }
    }

    public Location loadLeaderboardLocation(String leaderboardId) {
        String sql = "SELECT world, x, y, z, yaw, pitch FROM leaderboard_locations WHERE leaderboard_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, leaderboardId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String world = rs.getString("world");
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    float yaw = rs.getFloat("yaw");
                    float pitch = rs.getFloat("pitch");
                    
                    logger.info("Loaded location for leaderboard '" + leaderboardId + "': " + world + " at " + x + "," + y + "," + z);
                    
                    // Create Location object (need to get world from Bukkit)
                    org.bukkit.World bukkitWorld = org.bukkit.Bukkit.getWorld(world);
                    if (bukkitWorld != null) {
                        return new org.bukkit.Location(bukkitWorld, x, y, z, yaw, pitch);
                    } else {
                        logger.warning("World '" + world + "' not found for leaderboard '" + leaderboardId + "'");
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Error loading leaderboard location for '" + leaderboardId + "': " + e.getMessage());
        }
        
        return null;
    }

    public Map<String, Location> loadAllLeaderboardLocations() {
        Map<String, Location> locations = new HashMap<>();
        String sql = "SELECT leaderboard_id, world, x, y, z, yaw, pitch FROM leaderboard_locations";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String leaderboardId = rs.getString("leaderboard_id");
                String world = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");
                
                org.bukkit.World bukkitWorld = org.bukkit.Bukkit.getWorld(world);
                if (bukkitWorld != null) {
                    Location location = new org.bukkit.Location(bukkitWorld, x, y, z, yaw, pitch);
                    locations.put(leaderboardId, location);
                } else {
                    logger.warning("World '" + world + "' not found for leaderboard '" + leaderboardId + "'");
                }
            }
        } catch (SQLException e) {
            logger.severe("Error loading all leaderboard locations: " + e.getMessage());
        }
        
        return locations;
    }

}