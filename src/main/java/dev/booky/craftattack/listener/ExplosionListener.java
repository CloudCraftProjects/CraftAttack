package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (14:59 05.10.22)

import dev.booky.craftattack.CaManager;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class ExplosionListener implements Listener {

    private final CaManager manager;

    public ExplosionListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onExplosionDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Creeper) {
            double mul = this.manager.getConfig().getFeatures().getCreeperDamageMultiplier();
            event.setDamage(event.getDamage() * mul);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onExplosion(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper && this.manager.getConfig().getFeatures().isCreeperNoBlockDamage()) {
            event.blockList().clear();
        }
    }
}
