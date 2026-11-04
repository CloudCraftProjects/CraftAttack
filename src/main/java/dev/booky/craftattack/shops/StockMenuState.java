package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (21:10 21.10.2026)

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.util.NumberConversions;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class StockMenuState {

    private final ShopVillager shopVillager;
    private int page;

    StockMenuState(ShopVillager shopVillager) {
        shopVillager.ensureLoaded();
        this.shopVillager = shopVillager;
    }

    public boolean isValid(Player player) {
        if (!player.valid) {
            return false;
        }
        return this.shopVillager.getMerchantIfValid() != null;
    }

    public void lock() {
        this.shopVillager.getStateLock().lock();
    }

    public void unlock() {
        this.shopVillager.getStateLock().unlock();
    }

    public int getPageCount() {
        int pages = NumberConversions.ceil(this.shopVillager.getStockCount() / (double) ShopMenu.STOCK_CONTENT_SLOTS);
        return Math.min(ShopMenu.MAX_STOCK_PAGES, pages + 1);
    }

    private int getAbsoluteSlot(int slot) {
        return ShopMenu.STOCK_CONTENT_SLOTS * this.page + slot;
    }

    public ItemStack getStack(int slot) {
        if (slot < 0 || slot >= ShopMenu.STOCK_CONTENT_SLOTS) {
            throw new IndexOutOfBoundsException("Slot " + slot + " is out of bounds for stock menu");
        }
        int absoluteSlot = this.getAbsoluteSlot(slot);
        return this.shopVillager.getStockStack(absoluteSlot);
    }

    public void setStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= ShopMenu.STOCK_CONTENT_SLOTS) {
            throw new IndexOutOfBoundsException("Slot " + slot + " is out of bounds for stock menu");
        }
        int absoluteSlot = this.getAbsoluteSlot(slot);
        this.shopVillager.setStockStack(absoluteSlot, stack);
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = Math.clamp(page, 0, this.getPageCount());
    }

    public ShopVillager getShopVillager() {
        return this.shopVillager;
    }
}
