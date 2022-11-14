package dev.booky.craftattack.commands.admin.protections.flags;
// Created by booky10 in CraftAttack (19:54 14.11.22)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;

public class FlagSubCommand extends CommandAPICommand {

    public FlagSubCommand(CaManager manager) {
        super("flag");
        super.withPermission("craftattack.command.admin.protections.flag");

        super.withSubcommand(new FlagListCommand(manager));
        super.withSubcommand(new FlagAddCommand(manager));
        super.withSubcommand(new FlagRemoveCommand(manager));
    }
}
