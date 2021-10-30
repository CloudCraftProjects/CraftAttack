package tk.booky.craftattack.commands;
// Created by booky10 in CraftAttack (14:36 01.03.21)

import dev.jorel.commandapi.CommandAPICommand;
import tk.booky.craftattack.commands.admin.AdminSubCommand;
import tk.booky.craftattack.commands.teleport.TeleportSubCommand;
import tk.booky.craftattack.utils.CraftAttackManager;

public class CraftAttackRootCommand extends CommandAPICommand {

    public CraftAttackRootCommand(CraftAttackManager manager) {
        super("craftattack");

        withAliases("ca");
        withPermission("craftattack.command");

        withSubcommand(new TeleportSubCommand(manager));
        withSubcommand(new AdminSubCommand(manager));
    }
}
