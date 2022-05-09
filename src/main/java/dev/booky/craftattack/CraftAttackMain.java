package dev.booky.craftattack;

import dev.booky.craftattack.commands.CraftAttackRootCommand;
import dev.booky.craftattack.listener.ElytraListener;
import dev.booky.craftattack.listener.InteractListener;
import dev.booky.craftattack.listener.ProtectionListener;
import dev.booky.craftattack.listener.SitListener;
import dev.booky.craftattack.utils.CraftAttackConfig;
import dev.booky.craftattack.utils.CraftAttackManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.Bukkit.getServicesManager;
import static org.bukkit.plugin.ServicePriority.Normal;

public final class CraftAttackMain extends JavaPlugin {

    private CraftAttackConfig configuration;
    private CraftAttackManager manager;
    private CommandAPICommand command;

    @Override
    public void onLoad() {
        configuration = new CraftAttackConfig(new File(getDataFolder(), "config.yml"));
        (command = new CraftAttackRootCommand(manager = new CraftAttackManager(configuration, this))).register();
    }

    @Override
    public void onEnable() {
        manager.loadOverworld();

        configuration
            .reloadConfiguration()
            .saveConfiguration();

        getPluginManager().registerEvents(new ProtectionListener(manager), this);
        getPluginManager().registerEvents(new InteractListener(manager), this);
        getPluginManager().registerEvents(new ElytraListener(manager), this);
        getPluginManager().registerEvents(new SitListener(manager), this);

        getServicesManager().register(CraftAttackManager.class, manager, this, Normal);
    }

    @Override
    public void onDisable() {
        configuration.saveConfiguration();
        CommandAPI.unregister(command.getName(), true);
    }

    public CraftAttackConfig configuration() {
        return configuration;
    }

    public CraftAttackManager manager() {
        return manager;
    }

    public CommandAPICommand command() {
        return command;
    }
}
