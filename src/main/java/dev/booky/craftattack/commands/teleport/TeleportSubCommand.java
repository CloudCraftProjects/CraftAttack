package dev.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:50 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;

import static dev.booky.craftattack.utils.CaConstants.DIMENSION_END;
import static dev.booky.craftattack.utils.CaConstants.DIMENSION_NETHER;

public class TeleportSubCommand extends CommandAPICommand {

    public TeleportSubCommand(CaManager manager) {
        super("tp");
        super.withPermission("craftattack.command.teleport");

        super.withSubcommand(new TeleportBedCommand(manager));
        super.withSubcommand(new TeleportDimensionCommand(manager, "nether", DIMENSION_NETHER));
        super.withSubcommand(new TeleportDimensionCommand(manager, "end", DIMENSION_END));
        super.withSubcommand(new TeleportSpawnCommand(manager));
    }
}
