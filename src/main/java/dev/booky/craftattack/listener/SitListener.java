package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:40 24.05.21)

import dev.booky.craftattack.utils.CraftAttackManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityDismountEvent;

public class SitListener implements Listener {

    private final NamespacedKey chairKey;

    public SitListener(CraftAttackManager manager) {
        chairKey = new NamespacedKey(manager.plugin(), "chair");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getPlayer().isSneaking()) return;
        if (!event.getMaterial().isAir()) return;

        Block block = event.getClickedBlock();
        if (!block.getRelative(BlockFace.UP).getType().isAir()) return;

        Material material = block.getType();
        byte type;

        if (Tag.SLABS.isTagged(material)) {
            if (block.getBlockData() instanceof Slab slab && slab.getType() == Slab.Type.BOTTOM) {
                type = (byte) 1;
            } else {
                return;
            }
        } else if (Tag.STAIRS.isTagged(material)) {
            type = (byte) 2;
        } else {
            return;
        }

        Location location = block.getLocation().add(0.5, 0.3, 0.5);
        if (location.getNearbyEntitiesByType(ArmorStand.class, 0.5).size() != 0) return;

        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        PersistentDataContainer container = stand.getPersistentDataContainer();
        container.set(chairKey, PersistentDataType.BYTE, type);

        stand.addPassenger(event.getPlayer());
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setSmall(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material material = block.getType();

        if (!Tag.STAIRS.isTagged(material) && !Tag.SLABS.isTagged(material)) return;

        Location location = block.getLocation().add(0.5, 0.3, 0.5);
        for (ArmorStand stand : location.getNearbyEntitiesByType(ArmorStand.class, 0.5)) {
            if (stand.getPersistentDataContainer().has(chairKey, PersistentDataType.BYTE)) {
                stand.remove();
            }
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        Byte chairType = event.getDismounted().getPersistentDataContainer().get(chairKey, PersistentDataType.BYTE);
        if (chairType == null) return;

        Location location = event.getDismounted().getLocation().add(0, chairType / 2d, 0);
        location.setPitch(entity.getLocation().getPitch());
        location.setYaw(entity.getLocation().getYaw());

        if (!location.clone().add(0, entity.getEyeHeight(), 0).getBlock().getType().isCollidable()) {
            entity.teleport(location);
            event.getDismounted().remove();
        }

        event.getDismounted().getLocation().getBlock().breakNaturally(true);
        entity.teleport(entity.getLocation()
                .add(0, 1, 0)
                .toCenterLocation()
                .subtract(0, 0.375, 0));
        event.getDismounted().remove();
    }
}
