package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (13:20 06.08.21)

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPoseChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import tk.booky.craftattack.manager.CraftAttackManager;

public class ElytraJumperListener implements Listener {

    private static final Vector BOOST_VELOCITY = new Vector(0, 4, 0);
    private static final PotionEffect BOOST_EFFECT = new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 5, 255, false, false, false);

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.PHYSICAL)) {
            Block block = event.getClickedBlock();
            if (block != null && Tag.PRESSURE_PLATES.isTagged(block.getType())) {
                if (block.getRelative(0, -2, 0).getType().equals(Material.BARRIER)) {
                    event.getPlayer().setVelocity(BOOST_VELOCITY);
                    event.getPlayer().addPotionEffect(BOOST_EFFECT);

                    CraftAttackManager.giveElytra(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onElytraChange(EntityPoseChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            if (!event.getPose().equals(Pose.FALL_FLYING)) {
                CraftAttackManager.removeElytra((HumanEntity) event.getEntity());
            }
        }
    }

    @EventHandler
    public void onElytraDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (CraftAttackManager.hasElytra(event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (System.currentTimeMillis() - event.getPlayer().getFirstPlayed() < 500) {
            event.getPlayer().teleport(CraftAttackManager.getSpawnLocation());
        }
    }
}
