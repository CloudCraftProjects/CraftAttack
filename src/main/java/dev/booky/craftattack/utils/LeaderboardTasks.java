package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (03:17 26.10.2025)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig.LeaderboardConfig;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class LeaderboardTasks {

    private static final long UPDATE_INTERVAL = Ticks.TICKS_PER_SECOND * 42;

    private final CaManager manager;

    private final List<Integer> taskIds = new ArrayList<>();
    private final Map<UUID, LeaderboardConfig> configs = new HashMap<>();

    public LeaderboardTasks(CaManager manager) {
        this.manager = manager;
    }

    public void handleReload() {
        for (int taskId : this.taskIds) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        this.taskIds.clear();

        this.configs.clear();
        for (LeaderboardConfig config : this.manager.getConfig().getLeaderboards()) {
            this.configs.put(config.getEntityId(), config);
            if (Bukkit.getEntity(config.getEntityId()) instanceof TextDisplay display) {
                this.launchUpdater(display, config);
            }
        }
    }

    public void launchUpdater(TextDisplay display, LeaderboardConfig config) {
        LeaderboardTask lbTask = new LeaderboardTask(config, new WeakReference<>(display));
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.manager.getPlugin(), lbTask, 0L, UPDATE_INTERVAL);
        // save task id for cancelling later
        lbTask.setTaskId(task.getTaskId());
        this.taskIds.add(task.getTaskId());
    }

    public @Nullable LeaderboardConfig getConfig(UUID entityId) {
        return this.configs.get(entityId);
    }
}
