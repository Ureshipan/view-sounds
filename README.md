# View Sounds Mod

A client-side Minecraft Forge mod for version 1.20.1 that visualizes sounds with a grayscale overlay and sound circles when crouching.

## Features

- **Toggle Mode**: Press Y to enable/disable sound visualization
- **Crouch Activation**: When enabled, the mod activates when you crouch (Shift)
- **Grayscale Overlay**: Smooth transition to a grayscale filter when active
- **Sound Circles**: Debug circles appear at the source of detected sounds
- **Configurable**: Basic configuration system for future settings

## How to Use

1. **Enable the mod**: Press Y to toggle sound visualization on/off
2. **Activate visualization**: Crouch (hold Shift) while the mod is enabled
3. **View sounds**: White circles will appear at the source of any sounds with known positions
4. **Disable**: Either press Y again or stop crouching

## Installation

1. Download the mod JAR file
2. Place it in your `mods` folder
3. Make sure you have Forge 47.4.0+ for Minecraft 1.20.1 installed

## Development

This mod is currently in development. The sound circle positioning is simplified for debugging purposes and will be improved in future versions.

### Current Limitations

- Sound circle positioning is approximate (needs proper 3D to 2D projection)
- Only sounds with known world positions are tracked
- Basic circle rendering (will be improved with proper graphics)

## License

All Rights Reserved 