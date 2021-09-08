package tk.booky.craftattack;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import tk.booky.craftattack.commands.CraftAttackRootCommand;
import tk.booky.craftattack.listener.*;
import tk.booky.craftattack.manager.CraftAttackManager;

public final class CraftAttackMain extends JavaPlugin {

    public static CraftAttackMain main;

    @Override
    public void onEnable() {
        main = this;

        saveDefaultConfig();
        CraftAttackManager.load();

        Bukkit.getPluginManager().registerEvents(new ElytraJumperListener(), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new MiscListener(), this);
        Bukkit.getPluginManager().registerEvents(new SitListener(), this);

        new CraftAttackRootCommand().register();
    }

    @Override
    public void onDisable() {
        CraftAttackManager.save(false);
    }
}
