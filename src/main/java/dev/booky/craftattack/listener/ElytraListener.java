package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (13:20 06.08.21)

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.booky.craftattack.CaManager;
import dev.booky.launchplates.LaunchPlateManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Firework;
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
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

public final class ElytraListener implements Listener {

    private final CaManager manager;
    private final LaunchPlateManager plateManager;

    private final Cache<Player, Boolean> boostTimeout = CacheBuilder.newBuilder()
            .expireAfterWrite(1L, TimeUnit.SECONDS)
            .weakKeys()
            .build();

    public ElytraListener(CaManager manager) {
        this.manager = manager;

        RegisteredServiceProvider<LaunchPlateManager> plateRegistration = Bukkit.getServicesManager().getRegistration(LaunchPlateManager.class);
        this.plateManager = Objects.requireNonNull(plateRegistration).getProvider();
    }

    @EventHandler
    public void onElytraChange(EntityPoseChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getPose() == Pose.FALL_FLYING) {
            OptionalInt boosts = this.manager.getRemainingElytraBoosts(player);
            if (boosts.isPresent()) {
                player.sendActionBar(Component.translatable("ca.elytra.boosts-info", NamedTextColor.YELLOW,
                        Component.text(boosts.getAsInt(), NamedTextColor.WHITE)));
            }
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

        player.setFallDistance(0f);
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
            this.manager.setRemainingElytraBoosts(event.getPlayer(),
                    OptionalInt.of(this.manager.getConfig().getSpawnConfig().getElytraBoosts()));
            this.manager.giveElytra(event.getPlayer());
            return;
        }

        if (!this.manager.hasElytra(event.getPlayer())) {
            return;
        }

        // the player is still on ground for a tick sometimes
        if (System.currentTimeMillis() - this.plateManager.getLastLaunchUse(event.getPlayer()) < 100L) {
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShift(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return; // only fire when sneaking
        }
        Player player = event.getPlayer();
        if (!this.manager.hasElytra(player)) {
            return; // prevent potential abuse of this
        }
        if (!player.isGliding()) {
            return; // only boost gliding players
        }

        // only allow one boost per second
        if (this.boostTimeout.getIfPresent(player) != null) {
            return;
        }
        this.boostTimeout.put(player, true);

        OptionalInt newBoosts = this.manager.consumeElytraBoost(player);
        if (newBoosts.isEmpty()) {
            return; // no boosts remaining
        }

        player.sendActionBar(Component.translatable("ca.elytra.boosts-remaining", NamedTextColor.YELLOW,
                Component.text(newBoosts.getAsInt(), NamedTextColor.WHITE)));

        player.getWorld().spawn(player.getLocation(), Firework.class, false, firework -> {
            firework.setAttachedTo(player);
            firework.setShooter(player);
            firework.setTicksToDetonate(Ticks.TICKS_PER_SECOND
                    * this.manager.getConfig().getSpawnConfig().getElytraBoostDuration());

            firework.setVisibleByDefault(false);
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.canSee(player)) {
                    target.showEntity(this.manager.getPlugin(), firework);
                }
            }
        });
    }
}
