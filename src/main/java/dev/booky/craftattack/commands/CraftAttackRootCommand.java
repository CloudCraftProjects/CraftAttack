package dev.booky.craftattack.commands;
// Created by booky10 in CraftAttack (14:36 01.03.21)

import dev.booky.craftattack.commands.admin.AdminSubCommand;
import dev.booky.craftattack.commands.teleport.TeleportSubCommand;
import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;

public class CraftAttackRootCommand extends CommandAPICommand {

    public CraftAttackRootCommand(CaManager manager) {
        super("craftattack");

        withAliases("ca");
        withPermission("craftattack.command");

        withSubcommand(new TeleportSubCommand(manager));
        withSubcommand(new AdminSubCommand(manager));
        withSubcommand(new CraftCommand(manager));
    }
}
