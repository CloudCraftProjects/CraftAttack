package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (22:26 27.10.23)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig.MineStatEntry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class MineStatListener implements Listener {

    private final CaManager manager;

    public MineStatListener(CaManager manager) {
        this.manager = manager;
    }

    private PersistentDataContainer getContainer(Block block) {
        return block.getChunk().getPersistentDataContainer();
    }

    private NamespacedKey getBlockKey(Block block) {
        String strX = Integer.toString(block.getX() & 15);
        String strY = Integer.toString(block.getY());
        String strZ = Integer.toString(block.getZ() & 15);

        return new NamespacedKey(this.manager.getPlugin(),
                "stats/" + strX + "_" + strY + "_" + strZ);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        List<MineStatEntry> stats = this.manager.getConfig().getMineStats();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Block block = event.getBlock();
        for (MineStatEntry stat : stats) {
            if (!stat.hasBlock(block)) {
                continue;
            }

            if (stat.isPreventAbuse()) {
                PersistentDataContainer container = this.getContainer(block);
                NamespacedKey blockKey = this.getBlockKey(block);
                if (container.has(blockKey)) {
                    container.remove(blockKey);
                    continue; // placed by player
                }
            }

            Objective objective = scoreboard.getObjective(stat.getObjectiveName());
            if (objective != null) {
                Score score = objective.getScore(event.getPlayer());
                score.setScore(score.getScore() + 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        List<MineStatEntry> stats = this.manager.getConfig().getMineStats();

        Block block = event.getBlock();
        for (MineStatEntry stat : stats) {
            if (!stat.isPreventAbuse()) {
                continue;
            }

            if (stat.hasBlock(block)) {
                PersistentDataContainer container = this.getContainer(block);
                NamespacedKey blockKey = this.getBlockKey(block);
                container.set(blockKey, PersistentDataType.BOOLEAN, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        this.onPiston(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        this.onPiston(event.getBlocks(), event.getDirection());
    }

    private void onPiston(List<Block> blocks, BlockFace direction) {
        List<MineStatEntry> stats = this.manager.getConfig().getMineStats();
        for (Block block : blocks) {
            Block targetBlock = block.getRelative(direction);
            for (MineStatEntry stat : stats) {
                if (!stat.isPreventAbuse()) {
                    continue;
                }

                if (stat.hasBlock(block)) {
                    PersistentDataContainer container = this.getContainer(targetBlock);
                    NamespacedKey blockKey = this.getBlockKey(targetBlock);
                    container.set(blockKey, PersistentDataType.BOOLEAN, true);
                }
            }
        }
    }
}
