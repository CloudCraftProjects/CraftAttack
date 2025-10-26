package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (02:47 26.10.2025)

import org.bukkit.Bukkit;
import org.bukkit.entity.TextDisplay;
import org.jspecify.annotations.NullMarked;

import java.lang.ref.WeakReference;

@NullMarked
public final class LeaderboardTask implements Runnable {

    private final CaConfig.LeaderboardConfig config;
    private final WeakReference<TextDisplay> display;
    private int taskId;

    public LeaderboardTask(CaConfig.LeaderboardConfig config, WeakReference<TextDisplay> display) {
        this.config = config;
        this.display = display;
    }

    @Override
    public void run() {
        TextDisplay display = this.display.get();
        if (display == null || !display.isValid()) {
            Bukkit.getScheduler().cancelTask(this.taskId); // entity has been removed
        } else if (!Bukkit.getOnlinePlayers().isEmpty()) {
            LeaderboardUtil.applyLeaderboard(display, this.config);
        }
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
}
