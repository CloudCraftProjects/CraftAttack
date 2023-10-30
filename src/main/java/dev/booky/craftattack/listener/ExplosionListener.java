package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (14:59 05.10.22)

import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class ExplosionListener implements Listener {

    @EventHandler
    public void onExplosionDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Creeper) {
            event.setDamage(event.getDamage() / 2);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onExplosion(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper) {
            event.blockList().clear();
        }
    }
}
