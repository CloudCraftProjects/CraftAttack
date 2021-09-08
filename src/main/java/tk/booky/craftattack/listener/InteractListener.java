package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:02 01.03.21)

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import tk.booky.craftattack.manager.CraftAttackManager;

public class InteractListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK:
            case RIGHT_CLICK_BLOCK:
            case PHYSICAL:
                if (event.getClickedBlock() != null) {
                    if (CraftAttackManager.isInSpawn(event.getClickedBlock().getLocation(), event.getPlayer())) {
                        event.setCancelled(true);
                    }
                } else if (CraftAttackManager.isInSpawn(event.getPlayer())) {
                    event.setCancelled(true);
                }
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (CraftAttackManager.isInSpawn(event.getRightClicked().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
