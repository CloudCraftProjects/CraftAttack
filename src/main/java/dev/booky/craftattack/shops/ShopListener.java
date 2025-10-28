package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (23:26 26.10.2025)

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Ticker;
import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.UniqueIdDataType;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.view.MerchantView;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static net.kyori.adventure.text.Component.translatable;

@NullMarked
public final class ShopListener implements Listener {

    private final CaManager manager;
    private final NamespacedKey shopKey;
    private final NamespacedKey refKey;

    // don't always read/save data, add a cache for this
    private final LoadingCache<AbstractVillager, ShopVillager> villagerCache;
    private final Set<ShopVillager> villagerSaveQueue = new HashSet<>();

    public ShopListener(CaManager manager) {
        this.manager = manager;
        this.shopKey = new NamespacedKey(manager.getPlugin(), "shop");
        this.refKey = new NamespacedKey(manager.getPlugin(), "shop/reference");
        this.villagerCache = Caffeine.newBuilder()
                .weakKeys().ticker(Ticker.systemTicker()) // automatically remove expired entries
                .build(villager ->
                        new ShopVillager(manager, villager, this.villagerSaveQueue::add));
    }

    static void lookAt(LivingEntity source, LivingEntity target) {
        double diffX = target.getX() - source.getX();
        double diffY = (target.getY() + target.getEyeHeight()) - (source.getY() + source.getEyeHeight());
        double diffZ = target.getZ() - source.getZ();
        double distXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, distXZ));
        source.setRotation(yaw, pitch);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player
                && event.getEntity() instanceof AbstractVillager villager
                && villager.getPersistentDataContainer().has(this.shopKey)) {
            // make villager look at owner if punched
            ShopVillager shop = this.villagerCache.get(villager);
            if (shop.isOwner(player)) {
                lookAt(villager, player);
            }
            event.setDamage(0d);
        }
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
            boolean opened = ShopMenu.openMerchantMenu(this.manager.getPlugin(), shop, player);
            if (!opened) {
                player.sendMessage(CaManager.getPrefix().append(translatable("ca.menu.shop.no-trades")));
                if (shop.isOwner(player)) {
                    player.sendMessage(CaManager.getPrefix().append(translatable("ca.menu.shop.no-trades.owner-hit")));
                }
            }
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
        if (villager.getPersistentDataContainer().has(this.shopKey)) {
            // shop villager should never be traded with directly
            event.setCancelled(true);
            return;
        }
        UUID refId = villager.getPersistentDataContainer().get(this.refKey, UniqueIdDataType.INSTANCE);
        if (refId == null) {
            return; // not a shop villager merchant
        }
        Entity realVillager = villager.getWorld().getEntity(refId);
        if (!(realVillager instanceof AbstractVillager)
                || !realVillager.getPersistentDataContainer().has(this.shopKey)) {
            // referenced shop doesn't exist or is not a shop villager; cancel to be safe
            event.setCancelled(true);
            return;
        }
        // lookup shop villager instance and try to process this trade transaction
        ShopVillager shop = this.villagerCache.get((AbstractVillager) realVillager);
        boolean update;
        if (!shop.tryTrade(event.getTrade())) {
            event.setCancelled(true);
            update = true;
        } else {
            // trade was successful, but verify that there is still stock remaining; if not, trigger update
            update = !shop.isTradeStocked(event.getTrade());
        }

        // if e.g. the trade goes out of stock, trigger an update
        if (update) {
            Bukkit.getScheduler().runTask(this.manager.getPlugin(), () -> {
                // if the player still has the merchant inventory menu open, re-open it
                if (!(event.getPlayer().getOpenInventory() instanceof MerchantView)) {
                    return; // exited merchant inventory menu
                }
                if (!ShopMenu.openMerchantMenu(this.manager.getPlugin(), shop, event.getPlayer())) {
                    event.getPlayer().closeInventory(); // no trades present
                }
            });
        }
    }

    @EventHandler
    public void onTickEnd(ServerTickEndEvent event) {
        // players may bulk-buy a few items, so we have a queue for saving shop villager data to prevent lags
        if (!this.villagerSaveQueue.isEmpty()) {
            for (Iterator<ShopVillager> it = this.villagerSaveQueue.iterator(); it.hasNext(); ) {
                ShopVillager shop = it.next();
                it.remove();
                shop.saveData();
            }
        }
    }
}
