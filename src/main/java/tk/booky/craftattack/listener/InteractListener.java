package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:02 01.03.21)

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import tk.booky.craftattack.utils.CraftAttackManager;

public record InteractListener(CraftAttackManager manager) implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            switch (event.getAction()) {
                case LEFT_CLICK_BLOCK, RIGHT_CLICK_BLOCK -> {
                    if (manager.isProtected(event.getClickedBlock().getLocation(), event.getPlayer())) {
                        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENDER_CHEST) {
                            event.setCancelled(true);
                        }
                    }
                }
                case PHYSICAL -> {
                    if (manager.isProtected(event.getClickedBlock().getLocation(), null)) {
                        event.setCancelled(true);
                    }
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
        if (System.currentTimeMillis() - event.getPlayer().getFirstPlayed() < 500) {
            if (manager.config().spawnLocation() != null) {
                event.getPlayer().teleport(manager.config().spawnLocation());
            }
        }
    }
}
