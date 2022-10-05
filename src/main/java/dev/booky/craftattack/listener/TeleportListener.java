package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (15:02 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.TpResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CompletableFuture;

public final class TeleportListener implements Listener {

    private final CaManager manager;

    public TeleportListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasExplicitlyChangedBlock()) {
            return;
        }

        CompletableFuture<TpResult> future = this.manager.getTeleports().remove(event.getPlayer().getUniqueId());
        if (future != null) {
            event.getPlayer().sendMessage(CaManager.getPrefix().append(Component.translatable("ca.teleport.moved", NamedTextColor.RED)));
            future.complete(TpResult.CANCELLED);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        CompletableFuture<TpResult> future = this.manager.getTeleports().remove(event.getPlayer().getUniqueId());
        if (future != null) {
            future.complete(TpResult.DISCONNECTED);
        }
    }
}
