package dev.booky.craftattack;

import dev.booky.craftattack.commands.CaCommand;
import dev.booky.craftattack.listener.ElytraListener;
import dev.booky.craftattack.listener.EndListener;
import dev.booky.craftattack.listener.ExplosionListener;
import dev.booky.craftattack.listener.ProtectionListener;
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

    public CaMain() {
        try {
            Class.forName("io.papermc.paper.configuration.Configuration");
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException("Please use paper for this plugin to function! Download it at https://papermc.io/.");
        }
    }

    @Override
    public void onLoad() {
        this.manager = new CaManager(this, super.getDataFolder().toPath());
        new Metrics(this, 16590);

        if (Bukkit.getPluginManager().getPlugin("CommandAPI") == null) {
            super.getLogger().severe("###################################################################");
            super.getLogger().severe("# Install CommandAPI (https://commandapi.jorel.dev/) for commands #");
            super.getLogger().severe("###################################################################");
        } else {
            this.command = new CaCommand(this.manager);
        }
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
        if (Bukkit.getPluginManager().getPlugin("CommandAPI") != null) {
            CommandAPI.unregister(this.command.getName(), true);
        }
    }
}
