package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (16:07 19.05.21)

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPoseChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import tk.booky.craftattack.manager.CraftAttackManager;

public class ElytraListener implements Listener {

    @EventHandler
    public void onElytraChange(EntityPoseChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getPose().equals(Pose.FALL_FLYING)) return;
        if (CraftAttackManager.isInSpawn(event.getEntity().getLocation(), null)) return;
        CraftAttackManager.removeElytra((HumanEntity) event.getEntity());
    }

    @EventHandler
    public void onElytraDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!CraftAttackManager.hasElytra(event.getEntity())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (System.currentTimeMillis() - event.getPlayer().getFirstPlayed() < 500) {
            event.getPlayer().teleport(CraftAttackManager.getSpawnLocation());
        } else if (CraftAttackManager.isInSpawn(event.getPlayer().getLocation(), null)) {
            CraftAttackManager.giveElytra(event.getPlayer());
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onMove(PlayerMoveEvent event) {
        if (!event.getPlayer().isOnGround()) return;
        if (event.getPlayer().getPose().equals(Pose.FALL_FLYING)) return;
        if (CraftAttackManager.isInSpawn(event.getPlayer().getLocation(), null)) return;
        CraftAttackManager.removeElytra(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!CraftAttackManager.isInSpawn(event.getRespawnLocation(), null)) return;
        CraftAttackManager.giveElytra(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!CraftAttackManager.isInSpawn(event.getTo(), null)) return;
        CraftAttackManager.giveElytra(event.getPlayer());
    }
}