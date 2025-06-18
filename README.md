# Auto Bow Mod for Minecraft 1.21 Fabric

An advanced auto bow modification for Minecraft 1.21 that provides intelligent automated bow shooting with sophisticated anti-detection features.

## Features

### Core Functionality
- **Automatic Bow Shooting**: Detects bows in main hand or offhand and automatically shoots at randomized intervals
- **Advanced Randomization**: Gaussian distribution timing patterns with bias drift to avoid detection
- **AFK-Safe Operation**: Continues working when chat is open or when alt-tabbing

### User Interface
- **Real-time HUD Overlay**: Professional status display showing timing, durability, and ammunition
- **Settings GUI**: Complete configuration interface accessible via hotkey
- **Customizable Keybinds**: All controls remappable through Minecraft's controls menu

### Smart Protection
- **Durability Protection**: Automatically stops when bow durability gets too low
- **Ammunition Management**: Intelligent arrow tracking with creative mode and infinity support
- **Multi-hand Support**: Works with bows in either main hand or offhand

## Installation

### Prerequisites
- Minecraft 1.21
- Fabric Loader 0.16.4 or higher
- Fabric API 0.102.0+1.21 or higher
- Java 21

### Steps
1. Download the latest release from the page
2. Place the `.jar` file in your `mods` folder
3. Ensure Fabric API is also installed
4. Launch Minecraft with the Fabric profile

## Usage

### Default Controls
- **B Key**: Toggle auto bow on/off
- **N Key**: Force enable auto bow
- **M Key**: Check bow durability
- **O Key**: Open settings GUI

### Configuration
Access the settings GUI (default: O key) to customize:
- Draw time range (1-2 seconds default)
- Cooldown between shots
- Durability protection threshold
- Visual feedback options

## Technical Details

### Compatibility
- **Client-side only**: Works on most multiplayer servers
- **Cross-platform**: Windows, macOS, and Linux support
- **Performance optimized**: Minimal impact on game performance

### Anti-Detection Features
- Gaussian distribution timing patterns
- Pattern breaking algorithms
- Bias drift for long-term variation
- Micro-randomization for authenticity

## Development

### Building from Source
git clone https://github.com/yourusername/minecraft-auto-bow-mod.git
cd minecraft-auto-bow-mod
./gradlew build


### Development Environment
- Java 21
- IntelliJ IDEA (recommended)
- Fabric development environment

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

This mod is for educational and personal use. Always check server rules before using automation mods in multiplayer environments.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
