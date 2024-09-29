# CraftAttack (CloudCraft)

A minecraft paper plugin used for managing CraftAttack projects for the CloudCraft network.

## Features

- Spawn-Elytra handling with firework boosts included
  - Fireworks can be activated by sneaking
  - Can be configured using `/ca admin spawn elytra-box <corner1> <corner2>`
  - If present, [LaunchPlates](https://github.com/CloudCraftProjects/LaunchPlates/) will automatically equip a one-time elytra
- Status handling for tablist and player nametag
  - Can be configured in the configuration file and then loaded using `/ca admin reload`
  - Can be used using `/status`
  - Requires plugins like [CloudChat](https://github.com/CloudCraftProjects/CloudChat) to be shown
- Various administration utilities
  - Activate/deactivate end portals without restarts: `/ca admin end activate <true|false>`
  - Display large group of entities: `/ca admin mobcounts <entity>`
- Various player utilities
  - Spawn position warp command: `/spawn`
  - Respawn position warp command: `/bed`
  - End portal position warp command: `/end`
  - Open crafting table without placing one down: `/craft`
- Disabling creeper damage
- Chair-like sitting on stairs/slabs

## Download

https://dl.cloudcraftmc.de/download/tool/craftattack

**Note: Depends on [CommandAPI](https://commandapi.jorel.dev/) and [CloudCore](https://github.com/CloudCraftProjects/CloudCore/).**
Some features may require [LaunchPlates](https://github.com/CloudCraftProjects/LaunchPlates/) or [LuckPerms](https://luckperms.net/) to be installed.

## License

Licensed under GPL-3.0, see [LICENSE](./LICENSE) for further information.
