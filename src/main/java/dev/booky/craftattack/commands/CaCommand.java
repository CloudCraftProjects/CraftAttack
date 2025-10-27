package dev.booky.craftattack.commands;
// Created by booky10 in CraftAttack (14:36 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.commands.admin.AdminSubCommand;
import dev.booky.craftattack.commands.teleport.TeleportSubCommand;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;

public class CaCommand extends CommandAPICommand {

    public CaCommand(CaManager manager) {
        super("craftattack");
        this.withPermission("craftattack.command");
        this.withAliases("ca");

        this.withSubcommand(new TeleportSubCommand(manager));
        this.withSubcommand(new AdminSubCommand(manager));
        this.withSubcommand(new CraftCommand(manager));

        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            super.withSubcommand(new StatusCommand(manager));
        } else {
            Bukkit.getLogger().warning("LuckPerms is not installed, the status command won't work!");
        }

        this.withSubcommand(new MineStatsCommand());

        CommandAPI.unregister(this.getName(), true);
        for (String alias : this.getAliases()) {
            CommandAPI.unregister(alias, true);
        }

        this.register();
    }
}
