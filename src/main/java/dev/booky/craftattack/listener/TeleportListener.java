package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:02 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.TpResult;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class TeleportListener implements Listener {

    private final CaManager manager;

    public TeleportListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.hasExplicitlyChangedBlock()) {
            this.manager.cancelTeleport(event.getPlayer(), TpResult.CANCELLED);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.manager.cancelTeleport(event.getPlayer(), TpResult.DISCONNECTED);
    }
}
