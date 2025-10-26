package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (03:46 26.10.2025)

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig;
import dev.booky.craftattack.utils.LeaderboardTasks;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LeaderboardListener implements Listener {

    private final CaManager manager;

    public LeaderboardListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onEntityAdd(EntityAddToWorldEvent event) {
        if (!(event.getEntity() instanceof TextDisplay display)) {
            return; // not a text display
        }
        LeaderboardTasks tasks = this.manager.getLeaderboardTasks();
        CaConfig.LeaderboardConfig config = tasks.getConfig(display.getUniqueId());
        if (config != null) {
            tasks.launchUpdater(display, config);
        }
    }
}
