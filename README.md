# Board Plugin - Advanced Minecraft Leaderboards

A powerful Bukkit/Spigot plugin for creating customizable, persistent leaderboards with rich formatting options.

## Features

- 🏆 **Custom Leaderboards**: Create multiple leaderboards for different statistics
- 🎨 **Rich Formatting**: Full customization with gradients, colors, and custom formats
- 💾 **Persistent Storage**: Leaderboards automatically respawn after server restarts
- 🔄 **Auto Updates**: Configurable update intervals for real-time score tracking
- 🌍 **PlaceholderAPI Support**: Track any statistic using placeholders
- 📊 **Database Storage**: SQLite database for reliable score persistence

## Commands

- `/board spawn <leaderboard>` - Spawn a leaderboard at your location
- `/board remove <leaderboard>` - Remove a spawned leaderboard
- `/board update` - Manually trigger score updates
- `/board test <placeholder>` - Test placeholder resolution
- `/board debug` - Test updater functionality
- `/board help` - Show help information

## Configuration

### Basic Setup
```yaml
leaderboards:
  your_leaderboard:
    title: "Your Title"
    placeholder: "%your_placeholder%"
    prefix: "Custom Prefix"
```

### Advanced Formatting
```yaml
leaderboards:
  kills:
    title: "Kills"
    placeholder: "%player_total_kills%"
    prefix: "Combat Stats"
    
    # Text Formatting
    titleFormat: "<gradient:#ff0000:#ff6666>⚔️ {title} ⚔️</gradient>"
    footerFormat: "<gradient:#ff6666:#ff0000>────────────────────</gradient>"
    placeFormats:
      - "<color:#ff0000><bold>🥇 {place}.</bold></color> {head} <color:#ff0000>{name}</color> <gray>{score}</gray> <red>{suffix}</red>"
      - "<color:#ff6666><bold>🥈 {place}.</bold></color> {head} <color:#ff6666>{name}</color> <gray>{score}</gray> <red>{suffix}</red>"
      - "<color:#ff9999><bold>🥉 {place}.</bold></color> {head} <color:#ff9999>{name}</color> <gray>{score}</gray> <red>{suffix}</red>"
    defaultPlaceFormat: "<color:#ffcccc><bold>{place}.</bold></color> {head} <color:#ffcccc>{name}</color> <gray>{score}</gray> <red>{suffix}</red>"
    
    # Visual Settings
    leaderboardType: "TOP_PLAYER_HEAD"  # SIMPLE_TEXT, TOP_PLAYER_HEAD, ALL_PLAYER_HEADS
    rotationMode: "DYNAMIC"             # DYNAMIC, FIXED
    headMode: "ITEM_DISPLAY"           # ITEM_DISPLAY, RESOURCEPACK
    sortOrder: "DESCENDING"             # DESCENDING, ASCENDING
    
    # Layout Settings
    lineHeight: 0.25                   # Space between entries
    backgroundWidth: 40.0              # Width of background
    background: true                   # Show background
    backgroundColor: 0x54000000         # Background color (ARGB)
    
    # Display Settings
    maxDisplayEntries: 10              # Max entries to show
    showEmptyPlaces: true              # Show empty slots
    decimalNumbers: false              # Use decimal numbers
    numberLocale: "GERMANY"            # Number formatting locale
```

## Configuration Options

### Text Formatting
- `titleFormat`: Format for the leaderboard title
- `footerFormat`: Format for the leaderboard footer
- `placeFormats`: Array of formats for specific places (1st, 2nd, 3rd)
- `defaultPlaceFormat`: Format for all other places

### Visual Settings
- `leaderboardType`: Type of leaderboard display
- `rotationMode`: How player heads rotate
- `headMode`: How player heads are displayed
- `sortOrder`: Score sorting direction

### Layout Settings
- `lineHeight`: Space between entries
- `backgroundWidth`: Width of background panel
- `background`: Whether to show background
- `backgroundColor`: Background color (ARGB format)

### Display Settings
- `maxDisplayEntries`: Maximum number of entries to display
- `showEmptyPlaces`: Whether to show empty slots
- `decimalNumbers`: Whether to use decimal formatting
- `numberLocale`: Number formatting locale

## Dependencies

- **Spigot/Bukkit**: 1.19+ (tested on 1.19.4)
- **PlaceholderAPI**: Required for placeholder support
- **HologramLib**: Required for holographic display

## Installation

1. Download the latest release from [GitHub Releases](https://github.com/LadonON/Board/releases)
2. Place the `Board.jar` file in your `plugins/` directory
3. Install dependencies (PlaceholderAPI, HologramLib)
4. Restart your server
5. Configure the `config.yml` file
6. Use `/board spawn <leaderboard>` to create your first leaderboard

## Building from Source

```bash
# Clone the repository
git clone https://github.com/LadonON/Board.git
cd Board

# Build the plugin
./gradlew build

# The JAR file will be in build/libs/
```

## Permissions

- `board.spawn`: Spawn leaderboards
- `board.remove`: Remove leaderboards  
- `board.update`: Manually update scores
- `board.test`: Test placeholders
- `board.debug`: Debug functionality
- `board.help`: View help

## API

The Board plugin provides a simple API for developers:

```java
// Get the updater instance
Updater updater = ((Board) getPlugin("Board")).getUpdater();

// Get all leaderboard data
Map<String, GetLeaderboard.BoardData> boards = updater.getAllBoards();

// Get live leaderboards
Map<String, LeaderboardHologram> liveLeaderboards = updater.getLiveLeaderboards();
```

## Troubleshooting

### Leaderboard not spawning?
- Check if HologramLib is installed and working
- Verify PlaceholderAPI is installed if using placeholders
- Check console for error messages
- Try `/board debug` to test functionality

### Scores not updating?
- Ensure PlaceholderAPI placeholders are valid
- Check update interval in config
- Use `/board update` to manually trigger updates
- Verify database permissions

### Leaderboard disappeared after restart?
- Check if the database file exists and has proper permissions
- Verify the world name hasn't changed
- Use `/board spawn <leaderboard>` to recreate

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This plugin is released under the MIT License. See [LICENSE](LICENSE) for details.

## Support

- 📧 Issues: [GitHub Issues](https://github.com/LadonON/Board/issues)
- 💬 Discord: [Join our Discord](https://discord.gg/your-invite)
- 📖 Wiki: [Plugin Wiki](https://github.com/LadonON/Board/wiki)

## Changelog

### v1.0.0
- Initial release
- Custom leaderboard creation
- Persistent storage system
- Rich formatting options
- PlaceholderAPI integration
- SQLite database support

---

**Made with ❤️ by LadonON**
