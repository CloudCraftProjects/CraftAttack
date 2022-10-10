package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (13:20 06.08.21)

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import dev.booky.craftattack.CaManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPoseChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class ElytraListener implements Listener {

    private final CaManager manager;

    public ElytraListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onElytraChange(EntityPoseChangeEvent event) {
        if (event.getPose() == Pose.FALL_FLYING) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (this.manager.inElytraBox(player.getLocation())) {
            return;
        }
        if (!this.manager.noBoostSince(player, 100)) {
            return;
        }

        if (this.manager.removeElytra(player)) {
            player.setNoDamageTicks(20);
            player.setFallDistance(0f);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onElytraDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.isCancelled() || event.getFinalDamage() <= 0d) {
            return;
        }

        if (this.manager.inElytraBox(player.getLocation())) {
            return;
        }
        if (!this.manager.noBoostSince(player, 100)) {
            return;
        }

        if (this.manager.removeElytra(player)) {
            player.setFallDistance(0f);
            player.setNoDamageTicks(20);
            event.setDamage(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        if (this.manager.inElytraBox(event.getPlayer().getLocation())) {
            this.manager.giveElytra(event.getPlayer());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasExplicitlyChangedBlock()) {
            return;
        }
        if (event.getPlayer().getPose() == Pose.FALL_FLYING) {
            return;
        }

        // Yes, this is controlled by the client, but we don't care.
        // Our anticheat fixes this on the packet level.
        @SuppressWarnings("deprecation")
        boolean onGround = event.getPlayer().isOnGround();
        if (!onGround) {
            return;
        }

        if (this.manager.inElytraBox(event.getPlayer().getLocation())) {
            return;
        }
        if (!this.manager.noBoostSince(event.getPlayer(), 100)) {
            return;
        }

        if (this.manager.removeElytra(event.getPlayer())) {
            event.getPlayer().setNoDamageTicks(20);
            event.getPlayer().setFallDistance(0f);
        }
    }

    @EventHandler
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        if (this.manager.inElytraBox(event.getRespawnedLocation())) {
            this.manager.giveElytra(event.getPlayer());
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (this.manager.inElytraBox(event.getTo())) {
            this.manager.giveElytra(event.getPlayer());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        this.manager.removeElytra(event.getPlayer());
    }
}
