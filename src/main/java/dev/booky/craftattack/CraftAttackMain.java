package dev.booky.craftattack;

import dev.booky.craftattack.commands.CraftAttackRootCommand;
import dev.booky.craftattack.config.ConfigLoader;
import dev.booky.craftattack.listener.ElytraListener;
import dev.booky.craftattack.listener.InteractListener;
import dev.booky.craftattack.listener.ProtectionListener;
import dev.booky.craftattack.listener.SitListener;
import dev.booky.craftattack.utils.CaConfig;
import dev.booky.craftattack.utils.CraftAttackManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

import static org.bukkit.plugin.ServicePriority.Normal;

public final class CraftAttackMain extends JavaPlugin {

    private CraftAttackManager manager;
    private CommandAPICommand command;

    private Path configPath;
    private CaConfig config;

    @Override
    public void onLoad() {
        this.configPath = getDataFolder().toPath().resolve("config.yml");
        (command = new CraftAttackRootCommand(manager = new CraftAttackManager(this))).register();
    }

    @Override
    public void onEnable() {
        this.manager.loadOverworld();
        this.reloadCaConfig();

        Bukkit.getPluginManager().registerEvents(new ProtectionListener(manager), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(manager), this);
        Bukkit.getPluginManager().registerEvents(new ElytraListener(manager), this);
        Bukkit.getPluginManager().registerEvents(new SitListener(manager), this);

        Bukkit.getServicesManager().register(CraftAttackManager.class, manager, this, Normal);
    }

    @Override
    public void onDisable() {
        saveCaConfig();
        CommandAPI.unregister(command.getName(), true);
    }

    public void reloadCaConfig() {
        this.config = ConfigLoader.loadObject(this.configPath, CaConfig.class);
    }

    public void saveCaConfig() {
        ConfigLoader.saveObject(this.configPath, this.config);
    }

    public CaConfig getCaConfig() {
        return config;
    }

    public CraftAttackManager manager() {
        return manager;
    }

    public CommandAPICommand command() {
        return command;
    }
}
