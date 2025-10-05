package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (14:50 05.10.22)

import dev.booky.craftattack.CaManager;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class SpawnListener implements Listener {

    private final CaManager manager;

    public SpawnListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onSpawn(AsyncPlayerSpawnLocationEvent event) {
        if (!event.isNewPlayer()) {
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
