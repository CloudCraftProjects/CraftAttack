package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (13:20 06.08.21)

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import dev.booky.cloudcore.CloudManager;
import dev.booky.craftattack.CaManager;
import org.bukkit.Bukkit;
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
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Objects;

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

        if (!this.manager.hasElytra(player)) {
            return;
        }

        player.setNoDamageTicks(20);
        event.setDamage(0);

        if (!this.manager.inElytraBox(player.getLocation())) {
            this.manager.removeElytra(player);
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

        // this is controlled by the client, but an anticheat
        // should fix this on packet level
        @SuppressWarnings("deprecation")
        boolean onGround = event.getPlayer().isOnGround();
        if (!onGround) {
            return;
        }

        if (this.manager.inElytraBox(event.getPlayer().getLocation())) {
            this.manager.giveElytra(event.getPlayer());
            return;
        }

        if (!this.manager.hasElytra(event.getPlayer())) {
            return;
        }

        // the player is still on ground for a tick sometimes
        RegisteredServiceProvider<CloudManager> cloudRegistration = Bukkit.getServicesManager().getRegistration(CloudManager.class);
        CloudManager cloudManager = Objects.requireNonNull(cloudRegistration).getProvider();
        if (System.currentTimeMillis() - cloudManager.getLastLaunchUse(event.getPlayer()) < 100L) {
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
        } else {
            this.manager.removeElytra(event.getPlayer());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        this.manager.removeElytra(event.getPlayer());
    }
}
