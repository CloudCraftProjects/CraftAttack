package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (23:26 26.10.2025)

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Ticker;
import dev.booky.craftattack.CaManager;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
                .ticker(Ticker.systemTicker()) // automatically remove expired entries
                // don't keep this around for too long
                .weakKeys().expireAfterAccess(5L, TimeUnit.SECONDS)
                .build(villager ->
                        new ShopVillager(manager, villager, this.villagerSaveQueue::add));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTrade(PlayerTradeEvent event) {
        AbstractVillager villager = event.getVillager();
        if (!villager.getPersistentDataContainer().has(this.shopKey)) {
            return; // not a shop npc
        }
        List<ItemStack> ingredients = event.getTrade().getIngredients();
        if (ingredients.isEmpty()) {
            return; // free trade, no profit made
        }
        ShopVillager shop = this.villagerCache.get(villager);
        // don't adjust ingredient count, we just use raw ingredients here
        for (ItemStack ingredient : ingredients) {
            shop.addProfit(ingredient);
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
