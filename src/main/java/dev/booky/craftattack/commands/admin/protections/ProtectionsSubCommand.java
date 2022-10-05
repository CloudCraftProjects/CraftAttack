package dev.booky.craftattack.commands.admin.protections;
// Created by booky10 in CraftAttack (22:40 05.10.22)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;

public class ProtectionsSubCommand extends CommandAPICommand {

    public ProtectionsSubCommand(CaManager manager) {
        super("protections");
        super.withPermission("craftattack.command.admin.protections");

        super.withSubcommand(new ProtectionsListCommand(manager));
        super.withSubcommand(new ProtectionsCreateCommand(manager));
        super.withSubcommand(new ProtectionsDeleteCommand(manager));
    }
}
