package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (17:38 30.10.21)

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import dev.booky.craftattack.CaManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class ProtectionListener implements Listener {

    private final CaManager manager;

    public ProtectionListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (this.manager.isProtected(event.getBlock(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (this.manager.isProtected(event.getBlock(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (this.manager.isProtected(event.getBlock(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (this.manager.isProtected(event.getBlock(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (this.manager.isProtected(event.getEntity().getLocation(), null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (this.manager.isProtected(event.getEntity().getLocation(), null)) {
            switch (event.getSpawnReason()) {
                case JOCKEY, SPAWNER, VILLAGE_DEFENSE, VILLAGE_INVASION, REINFORCEMENTS, MOUNT, LIGHTNING,
                        TRAP, ENDER_PEARL, RAID, PATROL -> event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) {
        if (this.manager.isProtected(event.getBlock(), null)) {
            event.setNewCurrent(0);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        // Don't cancel regeneration food level change
        if (event.getFoodLevel() >= event.getEntity().getFoodLevel()) {
            return;
        }

        if (this.manager.isProtected(event.getEntity().getLocation(), event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK, RIGHT_CLICK_BLOCK -> {
                if (!this.manager.isProtected(event.getClickedBlock(), event.getPlayer())) {
                    return;
                }
                if (event.getClickedBlock() == null) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                }

                Material blockType = event.getClickedBlock().getType();
                if (blockType == Material.ENDER_CHEST) {
                    return;
                }
                if (!event.getMaterial().isBlock()) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    return;
                }

                // Try to not handle block places, because handling them here seems more "buggy"
                if (blockType.isInteractable() && !event.getPlayer().isSneaking()) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                }
            }
            case PHYSICAL -> {
                // We use null as an entity, because creative players should still not trample farmland
                if (this.manager.isProtected(event.getClickedBlock(), null)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (this.manager.isProtected(event.getRightClicked().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> this.manager.isProtected(block, null));
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> this.manager.isProtected(block, null));
    }

    @EventHandler
    public void onEntityRegainNegativeHealth(EntityRegainHealthEvent event) {
        // Paper doesn't want to fix it :(
        if (event.getAmount() < 0d) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (this.manager.isProtected(block, null)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (this.manager.isProtected(block, null)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPathfind(EntityPathfindEvent event) {
        if (this.manager.isProtected(event.getLoc(), null)) {
            event.setCancelled(true);
        }
    }
}
