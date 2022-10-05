package dev.booky.craftattack.commands.admin.end;
// Created by booky10 in Kingdoms (20:34 26.08.21)

import dev.booky.craftattack.utils.CraftAttackManager;
import dev.jorel.commandapi.CommandAPICommand;

public class EndSubCommand extends CommandAPICommand {

    public EndSubCommand(CraftAttackManager manager) {
        super("end");

        withPermission("craftattack.command.admin.end");

        withSubcommand(new GetLocationSubCommand(manager));
        withSubcommand(new SetLocationSubCommand(manager));
        withSubcommand(new ActivationSubCommand(manager));
    }
}
