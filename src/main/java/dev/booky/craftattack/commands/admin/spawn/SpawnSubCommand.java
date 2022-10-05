package dev.booky.craftattack.commands.admin.spawn;
// Created by booky10 in Kingdoms (20:34 26.08.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;

public class SpawnSubCommand extends CommandAPICommand {

    public SpawnSubCommand(CaManager manager) {
        super("spawn");

        withPermission("craftattack.command.admin.spawn");

        withSubcommand(new GetLocationSubCommand(manager));
        withSubcommand(new SetLocationSubCommand(manager));
    }
}
