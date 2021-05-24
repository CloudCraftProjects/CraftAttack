package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:40 24.05.21)

import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

public class SitListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasBlock() || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getClickedBlock() == null || !Tag.STAIRS.isTagged(event.getClickedBlock().getType())) return;

        Location location = event.getClickedBlock().getLocation();
        if (!location.add(0, 1, 0).getBlock().getType().isAir()) return;
        location.add(0.5, -0.5, 0.5);

        if (location.getNearbyEntitiesByType(ArmorStand.class, 0.5, stand -> stand.getScoreboardTags().contains("chair")).size() > 0) return;
        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);

        stand.addScoreboardTag("chair");
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setSmall(true);

        stand.addPassenger(event.getPlayer());
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getDismounted() instanceof ArmorStand)) return;
        if (!event.getDismounted().getScoreboardTags().contains("chair")) return;

        event.getDismounted().remove();
    }
}