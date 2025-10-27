package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (23:26 26.10.2025)

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Ticker;
import dev.booky.craftattack.CaManager;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.Set;

@NullMarked
public final class ShopListener implements Listener {

    private final CaManager manager;
    private final NamespacedKey shopKey;

    // don't always read/save data, add a cache for this
    private final LoadingCache<AbstractVillager, ShopVillager> villagerCache;
    private final Set<ShopVillager> villagerSaveQueue = new HashSet<>();

    public ShopListener(CaManager manager) {
        this.manager = manager;
        this.shopKey = new NamespacedKey(manager.getPlugin(), "shop");
        this.villagerCache = Caffeine.newBuilder()
                .weakKeys().ticker(Ticker.systemTicker()) // automatically remove expired entries
                .build(villager ->
                        new ShopVillager(manager, villager, this.villagerSaveQueue::add));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof AbstractVillager villager)
                || !villager.getPersistentDataContainer().has(this.shopKey)) {
            return; // not a shop villager
        }
        event.setCancelled(true);
        ShopVillager shop = this.villagerCache.get(villager);
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            ShopMenu.openMerchantMenu(shop, player);
            return;
        }
        // set player as owner if this shop doesn't have an owner yet
        if (shop.getOwnerId() == null) {
            shop.setOwnerId(player.getUniqueId());
        }
        if (shop.isOwner(player)) {
            ShopMenu.openMenu(shop, player);
        } else {
            player.playSound(villager, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTrade(PlayerTradeEvent event) {
        AbstractVillager villager = event.getVillager();
        if (!villager.getPersistentDataContainer().has(this.shopKey)) {
            return; // not a shop villager
        }
        ShopVillager shop = this.villagerCache.get(villager);
        if (!shop.tryTrade(event.getTrade())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTickEnd(ServerTickEndEvent event) {
        // players may bulk-buy a few items, so we have a queue for saving shop villager data to prevent lags
        if (!this.villagerSaveQueue.isEmpty()) {
            for (ShopVillager villager : this.villagerSaveQueue) {
                villager.saveData();
            }
            this.villagerSaveQueue.clear();
        }
    }
}
