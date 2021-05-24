package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:02 01.03.21)

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import tk.booky.craftattack.manager.CraftAttackManager;

public class InteractListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!CraftAttackManager.isInSpawn(event.getPlayer())) return;
        if (!event.getAction().equals(Action.PHYSICAL) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        if (CraftAttackManager.isInSpawn(event.getRightClicked().getLocation(), player)) {
            event.setCancelled(true);
        }
    }
}