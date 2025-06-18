# Auto Bow Mod for Minecraft 1.21 Fabric

## 🎯 Key Features

### Three Smart Operating Modes
- **🕒 Simple Mode**: Pure timed cycles (shoot X minutes, break Y minutes) - no interruptions, no daily limits
- **📊 Efficiency Mode**: Automatic breaks when XP rates drop below your threshold (default: 60%)
- **🧠 Learning Mode**: Server-adaptive optimization with pattern learning and automatic timing adjustments

### Real McMMO Integration
- **Detects bossbar progress format**: `[current / total xp]` - works with all McMMO configurations
- **Real-time XP rate monitoring** with 100k XP/min baseline (GOOD performance)
- **Automatic efficiency tracking** and diminishing returns detection
- **Performance ratings**: EXCELLENT (150k+), GOOD (100k+), FAIR (80k+), BELOW AVERAGE, POOR

### Professional GUI & HUD
- **Tabbed settings interface** with 6 organized sections (Timing, Movement, Simple Mode, Efficiency, Learning, HUD)
- **Customizable HUD** with 4 corner positioning options and scaling (50%-150%)
- **Mode-aware display** showing relevant information per operating mode
- **Real-time efficiency monitoring** with visual progress bars

### Advanced Automation Features
- **Network packet-based bow automation** for Minecraft 1.21 Fabric
- **Advanced randomization** with pattern detection avoidance
- **Movement variation control** (completely optional - OFF stays OFF)
- **Durability protection** with configurable thresholds
- **Multi-hand bow support** (main hand and offhand detection)

### Server Adaptation
- **Learns optimal timing patterns** for each server automatically
- **Adapts to server response patterns** and anti-cheat systems
- **Comprehensive session statistics** and daily tracking
- **Daily session limits** with smart reset functionality

## 📈 Efficiency-Based Session Management

Unlike simple timer-based mods, Auto Bow monitors your actual McMMO XP rates and automatically takes breaks when efficiency drops below your configured threshold. This ensures optimal XP farming while respecting server diminishing returns mechanics.

**Example**: If your XP rate drops from 120k/min to 60k/min (below 60% threshold), the mod automatically pauses for a configurable break period to restore full XP rates.

## 🛠 Installation

### Prerequisites
- Minecraft 1.21
- Fabric Loader 0.16.4 or higher
- Fabric API 0.102.0+1.21 or higher
- Java 21

### Download Options
- **[Modrinth](https://modrinth.com/mod/auto-bow-efficiency-based-mcmmo-farming)** (Recommended)
- **[GitHub Releases](https://github.com/59n/minecraft-auto-bow-mod/releases)**

### Steps
1. Download the latest release from your preferred platform
2. Place the `.jar` file in your `mods` folder
3. Ensure Fabric API is also installed
4. Launch Minecraft with the Fabric profile

## 🎮 Usage

### Default Controls
- **B Key**: Toggle auto bow on/off
- **N Key**: Force enable auto bow
- **M Key**: Check bow durability
- **O Key**: Open settings GUI

### Configuration
Access the tabbed settings GUI (default: O key) to customize:

#### Timing Tab
- Draw time range (customizable timing)
- Cooldown between shots
- Durability protection threshold

#### Movement Tab
- Movement variation control (OFF/Low/Medium/High)
- Advanced randomization options
- Complete user control (OFF stays OFF)

#### Simple Mode Tab
- Timed shooting/breaking cycles
- No XP monitoring or daily limits
- Pure automation without interruptions

#### Efficiency Tab
- Real McMMO XP monitoring
- Efficiency thresholds and session limits
- Performance baseline configuration

#### Learning Tab
- Server adaptation settings
- Pattern learning controls
- Automatic optimization features

#### HUD Tab
- Display position and scaling
- Element visibility toggles
- Performance indicator settings

## 📊 Perfect Performance Baseline

Based on extensive testing, the mod uses **100k XP/min as the "GOOD" baseline** for McMMO archery farming, with clear performance indicators to help you optimize your farming efficiency.

## 🔒 Safety Features

- **Durability protection** prevents bow breaking
- **Daily session limits** prevent excessive use (configurable)
- **Movement variation** is completely optional (many users prefer OFF)
- **Server adaptation warnings** prevent unwanted setting changes
- **Comprehensive logging** for troubleshooting

## 🛠 Technical Details

### Compatibility
- **Client-side only**: Works on most multiplayer servers where automation is permitted
- **Cross-platform**: Windows, macOS, and Linux support
- **Performance optimized**: Minimal impact on game performance
- **Mod Menu integration**: Config button opens settings GUI

### Anti-Detection Features
- Gaussian distribution timing patterns
- Pattern breaking algorithms
- Bias drift for long-term variation
- Micro-randomization for authenticity
- Optional movement variation

## 🚀 Development

### Building from Source
```bash
git clone https://github.com/59n/minecraft-auto-bow-mod.git
cd minecraft-auto-bow-mod
./gradlew build
```

### Development Environment
- Java 21
- IntelliJ IDEA (recommended)
- Fabric development environment
- Fabric API for testing

### Latest Changes (v1.2.1)
- Fixed infinite level-up detection loop causing log spam
- Resolved unrealistic 300k+ XP/min readings - now shows accurate ~100k rates
- Enhanced movement settings persistence - OFF stays OFF permanently
- Improved rate validation with realistic 200k XP/min cap
- Added 100k XP/min baseline system with performance ratings

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## ⚠️ Disclaimer

This mod is designed for servers where automation is permitted. Always check your server's rules before use. The mod includes comprehensive session management to promote responsible usage.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request or open an issue on GitHub.

## 🔗 Links

- **[Modrinth Page](https://modrinth.com/mod/auto-bow-efficiency-based-mcmmo-farming)**
- **[GitHub Repository](https://github.com/59n/minecraft-auto-bow-mod)**
- **[Issues & Bug Reports](https://github.com/59n/minecraft-auto-bow-mod/issues)**
- **[Latest Release](https://github.com/59n/minecraft-auto-bow-mod/releases/latest)**
