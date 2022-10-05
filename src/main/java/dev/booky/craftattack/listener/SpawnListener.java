package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (14:50 05.10.22)

import dev.booky.craftattack.CaManager;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.UUID;

public final class SpawnListener implements Listener {

    private static final PotionEffect BOOST_EFFECT = new PotionEffect(PotionEffectType.SLOW_FALLING,
            20 * 5, 255, false, false, false);
    private static final Vector BOOST_VELOCITY = new Vector(0d, 3.8d, 0d); // Maximum
    private final Object2LongMap<UUID> lastBoost = new Object2LongOpenHashMap<>() {{
        this.defaultReturnValue(0);
    }};

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

        // A moving piston on top marks a pressure plate as a jumping plate // TODO: add a command for placing this
        if (block.getRelative(BlockFace.UP).getType() != Material.MOVING_PISTON) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        event.setCancelled(true);

        // This ensures a player can only activate a pressure plate every second
        if (currentTime - this.lastBoost.getLong(event.getPlayer().getUniqueId()) > 1000) {
            event.getPlayer().setVelocity(BOOST_VELOCITY);
            event.getPlayer().addPotionEffect(BOOST_EFFECT);
            event.getPlayer().playSound(event.getPlayer(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
                    SoundCategory.AMBIENT, 1f, 0.75f);

            this.lastBoost.put(event.getPlayer().getUniqueId(), currentTime);
            this.manager.giveElytra(event.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (System.currentTimeMillis() - event.getPlayer().getFirstPlayed() >= 500) {
            return;
        }

        if (manager.getConfig().getSpawnConfig().getWarpLocation() != null) {
            event.getPlayer().teleport(manager.getConfig().getSpawnConfig().getWarpLocation());
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
