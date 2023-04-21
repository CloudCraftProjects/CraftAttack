package dev.booky.craftattack.commands;
// Created by booky10 in CraftAttack (14:36 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.commands.admin.AdminSubCommand;
import dev.booky.craftattack.commands.teleport.TeleportSubCommand;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;

public class CaCommand extends CommandAPICommand {

    public CaCommand(CaManager manager) {
        super("craftattack");
        super.withPermission("craftattack.command");
        super.withAliases("ca");

        super.withSubcommand(new TeleportSubCommand(manager));
        super.withSubcommand(new AdminSubCommand(manager));
        super.withSubcommand(new CraftCommand());

        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            super.withSubcommand(new StatusCommand(manager));
        } else {
            Bukkit.getLogger().warning("LuckPerms is not installed, the status command won't work!");
        }

        super.register();
    }
}
