package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (14:50 05.10.22)

import dev.booky.craftattack.CaManager;
import dev.booky.launchplates.events.LaunchPlateUseEvent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class SpawnListener implements Listener {

    private static final PotionEffect BOOST_EFFECT = new PotionEffect(PotionEffectType.SLOW_FALLING,
            20 * 5, 255, false, false, false);
    private final CaManager manager;

    public SpawnListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onLaunchPlateUse(LaunchPlateUseEvent event) {
        event.getPlayer().addPotionEffect(BOOST_EFFECT);
        event.getPlayer().playSound(event.getPlayer(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
                SoundCategory.AMBIENT, 1f, 0.75f);
        this.manager.giveElytra(event.getPlayer());
    }

    @EventHandler
    public void onSpawn(PlayerSpawnLocationEvent event) {
        if (event.getPlayer().hasPlayedBefore()) {
            return;
        }

        Location spawnWarp = this.manager.getConfig().getSpawnConfig().getWarpLocation();
        if (spawnWarp != null) {
            event.setSpawnLocation(spawnWarp);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (this.manager.getConfig().getSpawnConfig().getWarpLocation() == null) {
            return;
        }
        if (!event.isAnchorSpawn() && !event.isBedSpawn()) {
            event.setRespawnLocation(this.manager.getConfig().getSpawnConfig().getWarpLocation());
        }
    }
}
