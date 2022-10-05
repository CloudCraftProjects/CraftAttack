package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:40 24.05.21)

import dev.booky.craftattack.CaManager;
import io.papermc.paper.entity.RelativeTeleportFlag;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SitListener implements Listener {

    private static final Set<Material> VALID_TYPES = Stream.concat(Tag.STAIRS.getValues().stream(),
            Tag.SLABS.getValues().stream()).collect(Collectors.toUnmodifiableSet());
    private final NamespacedKey chairKey;

    public SitListener(CaManager manager) {
        this.chairKey = new NamespacedKey(manager.getPlugin(), "chair");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getPlayer().isSneaking()) {
            return;
        }

        // Could annoy players who are building roofs, so players must have nothing in their hand
        if (!event.getMaterial().isAir()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (!block.getRelative(BlockFace.UP).isEmpty()) {
            return;
        }

        BlockData data = block.getBlockData();
        float customYaw = 0f;
        if (data instanceof Slab slab) {
            if (slab.getType() != Slab.Type.BOTTOM) {
                return;
            }
        } else if (data instanceof Stairs stairs) {
            if (stairs.getHalf() == Bisected.Half.TOP) {
                return;
            }

            customYaw = switch (stairs.getFacing()) {
                case NORTH -> 0f;
                case EAST -> 90f;
                case SOUTH -> 180f;
                case WEST -> -90f;
                default -> throw new AssertionError();
            };
        } else {
            return;
        }

        Location location = block.getLocation().add(0.5d, 0.3d, 0.5d);
        if (location.getNearbyEntitiesByType(ArmorStand.class, 0.1d).size() != 0) {
            return;
        }

        location.setYaw(customYaw);
        location.getWorld().spawn(location, ArmorStand.class, stand -> {
            PersistentDataContainer container = stand.getPersistentDataContainer();
            container.set(this.chairKey, PersistentDataType.BYTE, (byte) 1);

            stand.addPassenger(event.getPlayer());
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setSmall(true);

            // Yes, this actually works for hiding the vehicle health bar
            // Additionally, I didn't notice any side effects!
            AttributeInstance maxHealth = stand.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            Objects.requireNonNull(maxHealth).setBaseValue(0d);
        });

        // Hides the "Press Left Shift to Dismount" message
        event.getPlayer().sendActionBar(Component.empty());
    }

    // Execute after all the cancelling has happened
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!VALID_TYPES.contains(event.getBlock().getType())) {
            return;
        }

        Location location = event.getBlock().getLocation().add(0.5d, 0.3d, 0.5d);
        for (ArmorStand stand : location.getNearbyEntitiesByType(ArmorStand.class, 0.1d)) {
            if (stand.getPersistentDataContainer().has(this.chairKey)) {
                stand.remove();
            }
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!event.getDismounted().getPersistentDataContainer().has(this.chairKey)) {
            return;
        }

        Block stairBlock = event.getDismounted().getLocation().getBlock();
        if (stairBlock.isEmpty() || event.getDismounted().isDead()) {
            Location unmountLoc = stairBlock.getLocation().add(0.5d, 0.1d, 0.5d);
            unmountLoc.setYaw(player.getLocation().getYaw());
            unmountLoc.setPitch(player.getLocation().getPitch());

            player.teleport(unmountLoc,
                    PlayerTeleportEvent.TeleportCause.PLUGIN, true, true,
                    RelativeTeleportFlag.YAW, RelativeTeleportFlag.PITCH);
            event.getDismounted().remove();
            return;
        }

        Collection<BoundingBox> collisionBoxes = stairBlock.getCollisionShape().getBoundingBoxes();
        double maxCollisionY = Double.MIN_VALUE;

        for (BoundingBox collisionBox : collisionBoxes) {
            if (collisionBox.getMaxY() > maxCollisionY) {
                maxCollisionY = collisionBox.getMaxY();
            }
        }

        Location unmountLoc = stairBlock.getLocation().add(0.5d, maxCollisionY + 0.1d, 0.5d);
        unmountLoc.setYaw(player.getLocation().getYaw());
        unmountLoc.setPitch(player.getLocation().getPitch());

        player.teleport(unmountLoc,
                PlayerTeleportEvent.TeleportCause.PLUGIN, true, true,
                RelativeTeleportFlag.YAW, RelativeTeleportFlag.PITCH);
        event.getDismounted().remove();
    }
}
