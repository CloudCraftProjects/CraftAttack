package dev.booky.craftattack.commands.admin;
// Created by booky10 in CraftAttack (15:03 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.commands.admin.dimension.DimensionSubCommand;
import dev.booky.craftattack.commands.admin.spawn.SpawnSubCommand;
import dev.jorel.commandapi.CommandAPICommand;

import static dev.booky.craftattack.utils.CaConstants.DIMENSION_END;
import static dev.booky.craftattack.utils.CaConstants.DIMENSION_END_ID;
import static dev.booky.craftattack.utils.CaConstants.DIMENSION_NETHER;
import static dev.booky.craftattack.utils.CaConstants.DIMENSION_NETHER_ID;

public class AdminSubCommand extends CommandAPICommand {

    public AdminSubCommand(CaManager manager) {
        super("admin");
        super.withPermission("craftattack.command.admin");

        super.withSubcommand(new DimensionSubCommand(manager, DIMENSION_NETHER_ID, DIMENSION_NETHER));
        super.withSubcommand(new DimensionSubCommand(manager, DIMENSION_END_ID, DIMENSION_END));
        super.withSubcommand(new SpawnSubCommand(manager));
        super.withSubcommand(new ReloadConfigCommand(manager));
        super.withSubcommand(new MobCountCommand());
    }
}
