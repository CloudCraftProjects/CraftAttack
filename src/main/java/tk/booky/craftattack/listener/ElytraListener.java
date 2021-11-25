package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (13:20 06.08.21)

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPoseChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import tk.booky.craftattack.utils.CraftAttackManager;

import java.util.UUID;

import static org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LAUNCH;
import static org.bukkit.SoundCategory.AMBIENT;
import static org.bukkit.potion.PotionEffectType.SLOW_FALLING;

public class ElytraListener implements Listener {

    private static final PotionEffect BOOST_EFFECT = new PotionEffect(SLOW_FALLING, 20 * 5, 255, false, false, false);
    private static final Vector BOOST_VELOCITY = new Vector(0, 4, 0);
    private final Object2LongMap<UUID> lastBoost = new Object2LongOpenHashMap<>();
    private final CraftAttackManager manager;

    public ElytraListener(CraftAttackManager manager) {
        this.lastBoost.defaultReturnValue(0);
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();
            if (block != null && Tag.PRESSURE_PLATES.isTagged(block.getType())) {
                if (block.getRelative(BlockFace.UP).getType() == Material.MOVING_PISTON) {
                    long currentTime = System.currentTimeMillis();
                    event.setCancelled(true);

                    if (currentTime - lastBoost.getLong(event.getPlayer().getUniqueId()) > 1000) {
                        event.getPlayer().setVelocity(BOOST_VELOCITY);
                        event.getPlayer().addPotionEffect(BOOST_EFFECT);

                        event.getPlayer().playSound(block.getLocation(), ENTITY_FIREWORK_ROCKET_LAUNCH, AMBIENT, Short.MAX_VALUE, 0.75f);
                        lastBoost.put(event.getPlayer().getUniqueId(), currentTime);
                        manager.giveElytra(event.getPlayer());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onElytraChange(EntityPoseChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getPose() != Pose.FALL_FLYING) {
                if (!manager.isInSpawn(event.getEntity().getLocation(), null)) {
                    manager.removeElytra((HumanEntity) event.getEntity());
                    ((Player) event.getEntity()).setNoDamageTicks(20);
                }
            }
        }
    }

    @EventHandler
    public void onElytraDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (manager.removeElytra(player)) {
                player.setNoDamageTicks(20);
                event.setDamage(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        if (manager.isInSpawn(event.getPlayer().getLocation(), null)) {
            manager.giveElytra(event.getPlayer());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.hasExplicitlyChangedBlock() && event.getPlayer().getPose() != Pose.FALL_FLYING) {
            @SuppressWarnings("deprecation") // Yes, this is controlled by the client, but we don't care
            boolean onGround = event.getPlayer().isOnGround();

            if (onGround && !manager.isInSpawn(event.getPlayer().getLocation(), null)) {
                event.getPlayer().setNoDamageTicks(20);
                manager.removeElytra(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!event.isAnchorSpawn() && !event.isBedSpawn()) {
            event.setRespawnLocation(manager.config().spawnLocation());
        }
    }

    @EventHandler
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        if (manager.isInSpawn(event.getRespawnedLocation(), null)) {
            manager.giveElytra(event.getPlayer());
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (manager.isInSpawn(event.getTo(), null)) {
            manager.giveElytra(event.getPlayer());
        }
    }
}
