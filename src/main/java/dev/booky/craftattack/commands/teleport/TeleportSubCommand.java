package dev.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:50 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;

public class TeleportSubCommand extends CommandAPICommand {

    public TeleportSubCommand(CaManager manager) {
        super("tp");
        super.withPermission("craftattack.command.teleport");

        super.withSubcommand(new TeleportBedCommand(manager));
        super.withSubcommand(new TeleportEndCommand(manager));
        super.withSubcommand(new TeleportSpawnCommand(manager));
    }
}
