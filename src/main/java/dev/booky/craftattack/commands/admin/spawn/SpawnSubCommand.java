package dev.booky.craftattack.commands.admin.spawn;
// Created by booky10 in Kingdoms (20:34 26.08.21)

import dev.jorel.commandapi.CommandAPICommand;
import dev.booky.craftattack.utils.CraftAttackManager;

public class SpawnSubCommand extends CommandAPICommand {

    public SpawnSubCommand(CraftAttackManager manager) {
        super("spawn");

        withPermission("craftattack.command.admin.spawn");

        withSubcommand(new GetLocationSubCommand(manager));
        withSubcommand(new SetLocationSubCommand(manager));
        withSubcommand(new GetRadiusSubCommand(manager));
        withSubcommand(new SetRadiusSubCommand(manager));
    }
}
