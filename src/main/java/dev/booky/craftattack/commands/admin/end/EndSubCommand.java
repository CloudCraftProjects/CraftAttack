package dev.booky.craftattack.commands.admin.end;
// Created by booky10 in Kingdoms (20:34 26.08.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;

public class EndSubCommand extends CommandAPICommand {

    public EndSubCommand(CaManager manager) {
        super("end");
        super.withPermission("craftattack.command.admin.end");

        super.withSubcommand(new EndWarpLocationGetCommand(manager));
        super.withSubcommand(new EndWarpLocationSetCommand(manager));
        super.withSubcommand(new EndActivationGetCommand(manager));
        super.withSubcommand(new EndActivationSetCommand(manager));
    }
}
