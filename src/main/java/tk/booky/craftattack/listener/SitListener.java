package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:40 24.05.21)

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import tk.booky.craftattack.CraftAttackMain;

import java.util.Collection;

public class SitListener implements Listener {

    public SitListener() {
        Bukkit.getScheduler().runTaskTimer(CraftAttackMain.main, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                    if (stand.getScoreboardTags().contains("chair") && stand.getPassengers().size() == 0) {
                        stand.remove();
                    }
                }
            }
        }, 40, 40);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasBlock() || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getClickedBlock() == null || !Tag.STAIRS.isTagged(event.getClickedBlock().getType())) return;
        if (event.getPlayer().isSneaking() || !event.getMaterial().isAir()) return;

        Location location = event.getClickedBlock().getLocation();
        if (!location.clone().add(0, 1, 0).getBlock().getType().isAir()) return;
        if (location.clone().subtract(0, 1, 0).getBlock().getType().isAir()) return;
        location.add(0.5, 0.5, 0.5);

        if (location.getNearbyEntitiesByType(ArmorStand.class, 0.5, stand -> stand.getScoreboardTags().contains("chair")).size() > 0) return;
        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);

        stand.addPassenger(event.getPlayer());

        Bukkit.getScheduler().runTaskLater(CraftAttackMain.main, () -> {
            stand.addScoreboardTag("chair");
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setSmall(true);
        }, 1);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block;
        if (Tag.STAIRS.isTagged(event.getBlock().getType())) {
            block = event.getBlock();
        } else {
            block = event.getBlock().getRelative(BlockFace.UP);
            if (!Tag.STAIRS.isTagged(block.getType())) {
                return;
            }
        }

        Location location = block.getLocation().toCenterLocation();
        Collection<ArmorStand> stands = location.getNearbyEntitiesByType(ArmorStand.class, 0.5, stand -> stand.getScoreboardTags().contains("chair"));
        stands.forEach(ArmorStand::remove);
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getDismounted() instanceof ArmorStand)) return;
        if (!event.getDismounted().getScoreboardTags().contains("chair")) return;

        Location location = event.getDismounted().getLocation().clone().add(0, 2, 0);
        location.setYaw(event.getEntity().getLocation().getYaw());
        location.setPitch(event.getEntity().getLocation().getPitch());
        event.getEntity().teleport(location);

        Bukkit.getScheduler().runTaskLater(CraftAttackMain.main, () -> {
            if (!event.getDismounted().isDead()) {
                event.getDismounted().remove();
            }
        }, 5);
    }
}
