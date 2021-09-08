package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (12:47 06.08.21)

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import tk.booky.craftattack.CraftAttackMain;
import tk.booky.craftattack.manager.CraftAttackManager;

public class DiamondEventListener implements Listener {

    private final Objective objective;

    public DiamondEventListener() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if ((objective = scoreboard.getObjective(DisplaySlot.PLAYER_LIST)) == null) {
            throw new IllegalStateException("No objective has been made to be shown in the playerlist!");
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (CraftAttackManager.isInSpawn(event.getBlock().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        } else if (CraftAttackManager.isInEnd(event.getBlock().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        } else {
            switch (event.getBlock().getType()) {
                case DEEPSLATE_DIAMOND_ORE:
                case DIAMOND_ORE:
                    if (!event.getBlock().hasMetadata("player_placed")) {
                        Score score = objective.getScore(event.getPlayer().getName());
                        score.setScore(score.isScoreSet() ? score.getScore() + 1 : 1);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onPiston(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            Material material = block.getType();
            if (material == Material.DEEPSLATE_DIAMOND_ORE || material == Material.DIAMOND_ORE) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (CraftAttackManager.isInSpawn(event.getBlock().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        } else if (CraftAttackManager.isInEnd(event.getBlock().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        } else {
            switch (event.getBlock().getType()) {
                case DEEPSLATE_DIAMOND_ORE:
                case DIAMOND_ORE:
                    event.getBlock().setMetadata("player_placed", new FixedMetadataValue(CraftAttackMain.main, true));
                    break;
                default:
                    break;
            }
        }
    }
}
