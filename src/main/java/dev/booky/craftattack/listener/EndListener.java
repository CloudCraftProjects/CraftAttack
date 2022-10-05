package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (14:58 05.10.22)

import dev.booky.craftattack.CaManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class EndListener implements Listener {

    private final CaManager manager;

    public EndListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onEndEnter(PlayerTeleportEvent event) {
        if (event.getTo().getWorld().getEnvironment() != World.Environment.THE_END) {
            return;
        }
        if (event.getPlayer().getAllowFlight()) {
            return;
        }

        if (!this.manager.getConfig().getEndConfig().isActivated()) {
            event.setCancelled(true);
        }
    }
}
