package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (16:07 25.10.2025)

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.TpResult;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.NumberConversions;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static dev.booky.craftattack.utils.CaConstants.DIMENSION_END;
import static dev.booky.craftattack.utils.CaConstants.DIMENSION_END_ID;
import static dev.booky.craftattack.utils.CaConstants.DIMENSION_NETHER;
import static dev.booky.craftattack.utils.CaConstants.DIMENSION_NETHER_ID;

@NullMarked
public class WarpPlateListener implements Listener {

    private final CaManager manager;

    private final Cache<UUID, Boolean> cooldown = Caffeine.newBuilder()
            .expireAfterAccess(500L, TimeUnit.MILLISECONDS)
            .build();

    public WarpPlateListener(CaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL || event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        BlockPosition pos = Position.block(block.getX(), block.getY(), block.getZ());
        if (pos.blockX() != NumberConversions.floor(player.getX())
                || pos.blockY() != NumberConversions.floor(player.getY())
                || pos.blockZ() != NumberConversions.floor(player.getZ())) {
            return; // only count players who are standing on the exact block of the plate
        }

        String target = this.manager.getConfig().getWarpPlateTarget(pos);
        if (target == null) {
            return;
        }

        // access cache before checking if the player is currently teleporting
        boolean onCooldown = this.cooldown.getIfPresent(event.getPlayer().getUniqueId()) != null;
        if (this.manager.hasTeleport(event.getPlayer()) || onCooldown) {
            return; // already teleporting or on cooldown
        }

        // TODO everything here is a horrible hack
        Location warpLoc = switch (target) {
            case DIMENSION_NETHER_ID, DIMENSION_END_ID -> this.manager.getConfig().getDimensionConfig(
                            DIMENSION_NETHER_ID.equals(target) ? DIMENSION_NETHER : DIMENSION_END)
                    .getWarpLocation();
            case "bed" -> {
                Location spawnLoc = player.getBedSpawnLocation();
                if (spawnLoc == null) {
                    spawnLoc = this.manager.getConfig().getSpawnConfig().getWarpLocation();
                }
                yield spawnLoc;
            }
            case "spawn" -> this.manager.getConfig().getSpawnConfig().getWarpLocation();
            default -> null;
        };
        if (warpLoc != null && player.hasPermission("craftattack.command.teleport." + target)) {
            this.cooldown.put(event.getPlayer().getUniqueId(), true);
            this.manager.teleportRequest(player, warpLoc).thenAccept(result -> {
                if (result == TpResult.SUCCESSFUL) {
                    player.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.teleport." + target, NamedTextColor.GREEN)));
                }
            });
        }
    }
}
