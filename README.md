# CraftAttack (CloudCraft)

A heavily configurable minecraft paper plugin for managing survival servers with vanilla-like features.

## Download

https://dl.cloudcraftmc.de/craftattack

> [!NOTE]
> Depends
> on [CommandAPI](https://modrinth.com/project/commandapi) and
> [CloudCore](https://modrinth.com/project/cloudcore).<br>
> Some features may require [LaunchPlates](https://modrinth.com/project/launchplates)
> or [LuckPerms](https://luckperms.net/) to be installed.

## Features

- Spawn-Elytra handling with firework boosts included
  - Fireworks can be activated by sneaking (configurable in config)
  - Can be configured using `/ca admin spawn elytra-box <corner1> <corner2>`
  - If present, [LaunchPlates](https://modrinth.com/project/launchplates) will automatically equip a one-time elytra
- Tag handling for tablist and player nametag
  - Tags can be created/edited/deleted by players themselves using `/tags` (or `/ca tag`)
  - Tags can only be edited/deleted by their creator or players with the `craftattack.command.tag.manage` permission
  - Any player can assign any tag to themselves using `/tags assign <tag>`
  - Requires plugins like [CloudChat](https://modrinth.com/project/cloudchat) to be shown
- Various administration utilities
  - Activate/deactivate end portals without restarts: `/ca admin end activate <true|false>`
  - Display large group of entities: `/ca admin mobcounts <entity>`
- Various player utilities
  - Spawn position warp command: `/spawn`
    - Can be set by admins using `/ca admin spawn location <x> <y> <z> [<yaw> <pitch>]`
  - Respawn position warp command: `/bed`
  - End portal position warp command: `/end`
    - Can be set by admins using `/ca admin end location <x> <y> <z> [<yaw> <pitch>]`
  - Open crafting table without placing one down: `/craft`
- Disabling creeper block/health damage (configurable in config)
- Chair-like sitting on stairs/slabs (configurable in config)
- Individual permissions for every single available command
  - None are given by default, use a plugin like [LuckPerms](https://luckperms.net/) to give players access to commands

## License

Licensed under GPL-3.0, see [LICENSE](./LICENSE) for further information.
