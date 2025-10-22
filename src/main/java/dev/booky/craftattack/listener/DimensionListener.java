package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (14:58 05.10.22)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig.DimensionConfig;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;

import static dev.booky.craftattack.utils.CaConstants.DIMENSION_END;
import static dev.booky.craftattack.utils.CaConstants.DIMENSION_NETHER;
import static dev.booky.craftattack.utils.CaConstants.DIMENSION_OVERWORLD;

public final class DimensionListener implements Listener {

    private final CaManager manager;

    public DimensionListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onDimensionEnter(PlayerTeleportEvent event) {
        if (event.getPlayer().getAllowFlight()) {
            return; // bypass
        } else if (event.getFrom().getWorld() == event.getTo().getWorld()) {
            return; // dimension didn't change
        }
        Key worldKey = event.getTo().getWorld().key();
        DimensionConfig config = this.manager.getConfig().getDimensionConfig(worldKey);
        if (!config.isActivated()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onNetherPortal(PortalCreateEvent event) {
        if (event.getReason() != PortalCreateEvent.CreateReason.FIRE) {
            return; // only handle lighting up nether portals
        }
        boolean toOverworld = event.getWorld().getEnvironment() == World.Environment.NETHER;
        Key targetDimension = toOverworld ? DIMENSION_OVERWORLD : DIMENSION_NETHER;
        DimensionConfig config = this.manager.getConfig().getDimensionConfig(targetDimension);
        if (!config.isActivated()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEndPortal(BlockPlaceEvent event) {
        if (event.getBlockAgainst().getType() != Material.END_PORTAL_FRAME
                || event.getItemInHand().getType() != Material.ENDER_EYE) {
            return; // not an end portal interaction
        }
        DimensionConfig config = this.manager.getConfig().getDimensionConfig(DIMENSION_END);
        if (!config.isActivated()) {
            event.setCancelled(true);
        }
    }
}
