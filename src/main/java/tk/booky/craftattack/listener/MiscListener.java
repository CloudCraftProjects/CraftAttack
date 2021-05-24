package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:02 01.03.21)

import org.bukkit.GameMode;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.*;
import tk.booky.craftattack.manager.CraftAttackManager;

public class MiscListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!event.getEntityType().equals(EntityType.BOAT)) {
            if (CraftAttackManager.isInSpawn(event.getEntity().getLocation(), null) || CraftAttackManager.isInEnd(event.getEntity().getLocation(), null)) {
                if (!(event instanceof EntityDamageByEntityEvent) || !((EntityDamageByEntityEvent) event).getDamager().getType().equals(EntityType.PLAYER) || !((Player) ((EntityDamageByEntityEvent) event).getDamager()).getGameMode().equals(GameMode.CREATIVE)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (CraftAttackManager.isInSpawn(event.getEntity().getLocation(), null)) {
            switch (event.getSpawnReason()) {
                case CUSTOM:
                case CURED:
                case EGG:
                case INFECTION:
                case ENDER_PEARL:
                case OCELOT_BABY:
                case SLIME_SPLIT:
                case SPAWNER_EGG:
                case DEFAULT:
                    break;
                default:
                    event.setCancelled(true);
                    break;
            }
        }
    }

    @EventHandler
    public void onBreed(EntityBreedEvent event) {
        if (event.getBreeder() == null) return;
        CraftAttackManager.addBreeds(event.getBreeder().getUniqueId(), 1);
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper) {
            event.blockList().clear();
        } else if (CraftAttackManager.isInSpawn(event.getLocation(), null, CraftAttackManager.getSpawnRadius() + 10)) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) {
        if (CraftAttackManager.isInSpawn(event.getBlock().getLocation(), null)) event.setNewCurrent(0);
    }
}