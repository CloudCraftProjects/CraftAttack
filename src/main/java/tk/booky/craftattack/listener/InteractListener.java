package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:02 01.03.21)

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
                    if (manager.isInSpawn(event.getClickedBlock().getLocation(), event.getPlayer())) {
                        event.setCancelled(true);
                    }
                }
                case PHYSICAL -> {
                    if (manager.isInSpawn(event.getClickedBlock().getLocation(), null)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (manager.isInSpawn(event.getRightClicked().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (System.currentTimeMillis() - event.getPlayer().getFirstPlayed() < 500) {
            event.getPlayer().teleport(manager.config().spawnLocation());
        }
    }
}
