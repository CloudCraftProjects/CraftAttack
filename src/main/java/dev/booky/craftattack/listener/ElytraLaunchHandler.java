package dev.booky.craftattack.listener;
// Created by booky10 in CraftAttack (05:20 29.09.2024)

import com.github.benmanes.caffeine.cache.Cache;
import dev.booky.craftattack.CaManager;
import dev.booky.launchplates.events.LaunchPlateUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ElytraLaunchHandler {

    public static void tryRegister(CaManager manager, Cache<Player, Boolean> launchPlateDelay) {
        if (Bukkit.getPluginManager().getPlugin("LaunchPlates") != null) {
            Listener listener = new Listener(manager, launchPlateDelay);
            Bukkit.getPluginManager().registerEvents(listener, manager.getPlugin());
        }
    }

    static final class Listener implements org.bukkit.event.Listener {

        private static final PotionEffect BOOST_EFFECT = new PotionEffect(PotionEffectType.SLOW_FALLING,
                20 * 5, 255, false, false, false);

        private final CaManager manager;
        private final Cache<Player, Boolean> launchPlateDelay;

        public Listener(CaManager manager, Cache<Player, Boolean> launchPlateDelay) {
            this.manager = manager;
            this.launchPlateDelay = launchPlateDelay;
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onLaunchPlateUse(LaunchPlateUseEvent event) {
            if (this.launchPlateDelay.getIfPresent(event.getPlayer()) != null) {
                event.setCancelled(true);
                return;
            }
            this.launchPlateDelay.put(event.getPlayer(), true);

            event.getPlayer().addPotionEffect(BOOST_EFFECT);
            event.getPlayer().playSound(event.getPlayer(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
                    SoundCategory.AMBIENT, 1f, 0.75f);
            this.manager.giveElytra(event.getPlayer());
        }
    }
}
