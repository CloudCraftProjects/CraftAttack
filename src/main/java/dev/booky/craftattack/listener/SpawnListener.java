package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (14:50 05.10.22)

import dev.booky.craftattack.CaManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class SpawnListener implements Listener {

    private static final PotionEffect BOOST_EFFECT = new PotionEffect(PotionEffectType.SLOW_FALLING,
            20 * 5, 255, false, false, false);
    private static final Vector BOOST_VELOCITY = new Vector(0d, 3.8d, 0d); // Maximum

    private final CaManager manager;

    public SpawnListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Physical interact is stepping on farmland or activating tripwire/redstone ore/pressure plate
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !Tag.PRESSURE_PLATES.isTagged(block.getType())) {
            return;
        }

        if (this.manager.getConfig().getLaunchPlates().contains(block.getLocation())) {
            event.setCancelled(true);

            // This ensures a player can only activate a pressure plate every second
            if (this.manager.noBoostSince(event.getPlayer(), 1000)) {
                event.getPlayer().setVelocity(BOOST_VELOCITY);
                event.getPlayer().addPotionEffect(BOOST_EFFECT);
                event.getPlayer().playSound(event.getPlayer(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
                        SoundCategory.AMBIENT, 1f, 0.75f);

                this.manager.setLastBoost(event.getPlayer());
                this.manager.giveElytra(event.getPlayer());
            }
        }
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
