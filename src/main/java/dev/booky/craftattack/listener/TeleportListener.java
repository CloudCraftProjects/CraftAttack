package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:02 01.03.21)

import dev.booky.craftattack.CaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public final class TeleportListener implements Listener {

    private final CaManager manager;

    public TeleportListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasExplicitlyChangedBlock()) {
            return;
        }

        BukkitTask task = this.manager.getTeleportRunnables().remove(event.getPlayer().getUniqueId());
        if (task != null) {
            this.manager.fail(event.getPlayer(), "You have moved, cancelled teleport");
            task.cancel();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BukkitTask task = this.manager.getTeleportRunnables().remove(event.getPlayer().getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
}
