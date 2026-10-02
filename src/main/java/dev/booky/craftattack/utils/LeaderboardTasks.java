package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (03:17 26.10.2025)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig.LeaderboardConfig;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.entity.TextDisplay;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public class LeaderboardTasks {

    private static final long UPDATE_INTERVAL = Ticks.TICKS_PER_SECOND * 42;

    private final CaManager manager;

    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final Map<UUID, LeaderboardConfig> configs = new ConcurrentHashMap<>();

    public LeaderboardTasks(CaManager manager) {
        this.manager = manager;
    }

    public void handleReload() {
        for (ScheduledTask task : this.tasks.values()) {
            task.cancel();
        }
        this.tasks.clear();
        this.configs.clear();
        for (LeaderboardConfig config : this.manager.getConfig().getLeaderboards()) {
            this.configs.put(config.getEntityId(), config);
            if (Bukkit.getEntity(config.getEntityId()) instanceof TextDisplay display) {
                this.launchUpdater(display, config);
            }
        }
    }

    public void launchUpdater(TextDisplay display, LeaderboardConfig config) {
        // cancel any updater already registered for this entity
        ScheduledTask oldTask = this.tasks.remove(config.getEntityId());
        if (oldTask != null) {
            oldTask.cancel();
        }
        ScheduledTask task = display.getScheduler().runAtFixedRate(this.manager.getPlugin(), schedTask -> {
            if (!display.isValid()) {
                schedTask.cancel();
                this.tasks.remove(config.getEntityId());
                return;
            }
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                LeaderboardUtil.applyLeaderboard(display, config);
            }
        }, () -> this.tasks.remove(config.getEntityId()), 1L, UPDATE_INTERVAL);
        if (task != null) {
            this.tasks.put(config.getEntityId(), task);
        }
    }

    public @Nullable LeaderboardConfig getConfig(UUID entityId) {
        return this.configs.get(entityId);
    }
}
