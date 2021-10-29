package tk.booky.craftattack.commands;
// Created by booky10 in CraftAttack (14:36 01.03.21)

import dev.jorel.commandapi.CommandAPICommand;
import tk.booky.craftattack.commands.admin.AdminSubCommand;
import tk.booky.craftattack.commands.teleport.TeleportSubCommand;

public class CraftAttackRootCommand extends CommandAPICommand {

    public CraftAttackRootCommand() {
        super("craftattack");

        withAliases("ca");
        withPermission("craftattack.command");

        withSubcommand(new TeleportSubCommand());
        withSubcommand(new AdminSubCommand());
    }
}
