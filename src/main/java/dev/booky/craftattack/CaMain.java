package dev.booky.craftattack;

import dev.booky.cloudcore.util.TranslationLoader;
import dev.booky.craftattack.commands.CaCommand;
import dev.booky.craftattack.listener.ElytraListener;
import dev.booky.craftattack.listener.EndListener;
import dev.booky.craftattack.listener.ExplosionListener;
import dev.booky.craftattack.listener.SitListener;
import dev.booky.craftattack.listener.SpawnListener;
import dev.booky.craftattack.listener.TeleportListener;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class CaMain extends JavaPlugin {

    private CommandAPICommand command;
    private CaManager manager;
    private TranslationLoader i18n;

    @Override
    public void onLoad() {
        this.manager = new CaManager(this, super.getDataFolder().toPath());
        new Metrics(this, 16590);

        this.i18n = new TranslationLoader(this);
        this.i18n.load();
    }

    @Override
    public void onEnable() {
        this.manager.reloadConfig();

        Bukkit.getPluginManager().registerEvents(new ElytraListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new EndListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new ExplosionListener(), this);
        Bukkit.getPluginManager().registerEvents(new SitListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new SpawnListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new TeleportListener(this.manager), this);

        Bukkit.getServicesManager().register(CaManager.class, this.manager, this, ServicePriority.Normal);

        this.command = new CaCommand(this.manager);
    }

    @Override
    public void onDisable() {
        if (this.manager != null) {
            this.manager.saveConfig();
        }

        if (this.i18n != null) {
            this.i18n.unload();
        }

        if (this.command != null) {
            CommandAPI.unregister(this.command.getName(), true);

            // Additional command "aliases"
            CommandAPI.unregister("spawn", true);
            CommandAPI.unregister("craft", true);
        }
    }
}
