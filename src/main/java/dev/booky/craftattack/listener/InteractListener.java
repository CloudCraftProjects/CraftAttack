package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:02 01.03.21)

import dev.booky.craftattack.utils.CraftAttackManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public record InteractListener(CraftAttackManager manager) implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasExplicitlyChangedBlock()) return;

        BukkitTask task = manager.teleportRunnables().remove(event.getPlayer().getUniqueId());
        if (task != null) {
            manager.fail(event.getPlayer(), "You have moved, cancelled teleport");
            task.cancel();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BukkitTask task = manager.teleportRunnables().remove(event.getPlayer().getUniqueId());
        if (task != null) task.cancel();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK, RIGHT_CLICK_BLOCK -> {
                if (!manager.isProtected(event.getClickedBlock().getLocation(), event.getPlayer())) return;
                if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENDER_CHEST) {
                    event.setCancelled(true);
                }
            }
            case PHYSICAL -> {
                if (manager.isProtected(event.getClickedBlock().getLocation(), null)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (manager.isProtected(event.getRightClicked().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (System.currentTimeMillis() - event.getPlayer().getFirstPlayed() >= 500) return;

        if (manager.config().spawnLocation() != null) {
            event.getPlayer().teleport(manager.config().spawnLocation());
        }
    }
}
