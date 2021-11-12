package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (17:38 30.10.21)

import org.bukkit.World.Environment;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import tk.booky.craftattack.utils.CraftAttackManager;

public record ProtectionListener(CraftAttackManager manager) implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (manager.isProtected(event.getBlock().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (manager.isProtected(event.getBlock().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucket(PlayerBucketFillEvent event) {
        if (manager.isProtected(event.getBlock().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        if (manager.isProtected(event.getBlock().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (manager.isProtected(event.getEntity().getLocation(), null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (manager.isProtected(event.getEntity().getLocation(), null)) {
            switch (event.getSpawnReason()) {
                case JOCKEY, SPAWNER, VILLAGE_DEFENSE, VILLAGE_INVASION, REINFORCEMENTS, MOUNT, LIGHTNING,
                    TRAP, ENDER_PEARL, RAID, PATROL -> event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplosionDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Creeper) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper) {
            event.blockList().clear();
        } else {
            event.blockList().removeIf(block -> manager.isProtected(block.getLocation(), null));
        }
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) {
        if (manager.isProtected(event.getBlock().getLocation(), null)) {
            event.setNewCurrent(0);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getFoodLevel() < event.getEntity().getFoodLevel()) {
            if (manager.isProtected(event.getEntity().getLocation(), event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEndEnter(PlayerTeleportEvent event) {
        if (event.getTo().getWorld().getEnvironment() == Environment.THE_END && !event.getPlayer().getAllowFlight()) {
            if (!manager.config().endActivated()) {
                event.setCancelled(true);
            }
        }
    }
}
