package dev.booky.craftattack;

import dev.booky.craftattack.commands.CraftAttackRootCommand;
import dev.booky.craftattack.listener.ElytraListener;
import dev.booky.craftattack.listener.EndListener;
import dev.booky.craftattack.listener.ExplosionListener;
import dev.booky.craftattack.listener.ProtectionListener;
import dev.booky.craftattack.listener.SitListener;
import dev.booky.craftattack.listener.SpawnListener;
import dev.booky.craftattack.listener.TeleportListener;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class CaMain extends JavaPlugin {

    private CommandAPICommand command;
    private CaManager manager;

    @Override
    public void onLoad() {
        this.manager = new CaManager(this, getDataFolder().toPath());
        this.command = new CraftAttackRootCommand(this.manager);
        this.command.register();
    }

    @Override
    public void onEnable() {
        this.manager.reloadConfig();

        Bukkit.getPluginManager().registerEvents(new ElytraListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new EndListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new ExplosionListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new SitListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new SpawnListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new TeleportListener(this.manager), this);

        Bukkit.getServicesManager().register(CaManager.class, this.manager, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        this.manager.saveConfig();
        CommandAPI.unregister(this.command.getName(), true);
    }
}
