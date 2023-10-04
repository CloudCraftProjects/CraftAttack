package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (14:50 05.10.22)

import dev.booky.craftattack.CaManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class SpawnListener implements Listener {

    private final CaManager manager;

    public SpawnListener(CaManager manager) {
        this.manager = manager;
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
