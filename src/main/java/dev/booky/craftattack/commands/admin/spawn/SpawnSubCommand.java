package dev.booky.craftattack.commands.admin.spawn;
// Created by booky10 in Kingdoms (20:34 26.08.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;

public class SpawnSubCommand extends CommandAPICommand {

    public SpawnSubCommand(CaManager manager) {
        super("spawn");
        super.withPermission("craftattack.command.admin.spawn");

        super.withSubcommand(new SpawnWarpLocationGetCommand(manager));
        super.withSubcommand(new SpawnWarpLocationSetCommand(manager, true));
        super.withSubcommand(new SpawnWarpLocationSetCommand(manager, false));
        super.withSubcommand(new SpawnElytraBoxGetCommand(manager));
        super.withSubcommand(new SpawnElytraBoxSetCommand(manager));
    }
}
