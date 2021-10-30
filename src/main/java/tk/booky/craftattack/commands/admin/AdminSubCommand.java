package tk.booky.craftattack.commands.admin;
// Created by booky10 in CraftAttack (15:03 01.03.21)

import dev.jorel.commandapi.CommandAPICommand;
import tk.booky.craftattack.commands.admin.end.EndSubCommand;
import tk.booky.craftattack.commands.admin.spawn.SpawnSubCommand;
import tk.booky.craftattack.utils.CraftAttackManager;

public class AdminSubCommand extends CommandAPICommand {

    public AdminSubCommand(CraftAttackManager manager) {
        super("admin");
        withPermission("craftattack.command.admin");

        withSubcommand(new EndSubCommand(manager));
        withSubcommand(new SpawnSubCommand(manager));
        withSubcommand(new StartSubCommand(manager));
    }
}
