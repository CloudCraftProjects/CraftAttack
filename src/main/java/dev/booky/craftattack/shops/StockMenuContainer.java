package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (1:11 PM 04.11.2026)

import com.google.common.collect.Lists;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.booky.craftattack.menu.AbstractMenu.SLOTS_PER_ROW;
import static dev.booky.craftattack.shops.ShopMenu.STOCK_CONTENT_SLOTS;
import static dev.booky.craftattack.shops.ShopMenu.STOCK_ROWS;

@NullMarked
class StockMenuContainer implements Container {

    private final StockMenuState state;

    private final List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = MAX_STACK;

    private final List<Slot> slots = new ArrayList<>();
    private final List<ItemStack> stacks = Lists.transform(this.slots, Slot::getItem);

    public StockMenuContainer(ShopVillager shop, @Nullable Runnable onBack) {
        this.state = new StockMenuState(shop);
        for (int i = 0; i < STOCK_CONTENT_SLOTS; i++) {
            this.slots.add(new StockMenuContentSlot(this, i, this.state));
        }
        this.slots.add(new StockMenuPageSlot(this, STOCK_CONTENT_SLOTS, this.state, -1, onBack));
        for (int i = STOCK_CONTENT_SLOTS + 1; i < STOCK_CONTENT_SLOTS + 8; i++) {
            this.slots.add(new StockMenuDummySlot(this, i));
        }
        this.slots.add(new StockMenuPageSlot(this, STOCK_CONTENT_SLOTS + 8, this.state, 1, null));
        assert this.slots.size() == STOCK_ROWS * SLOTS_PER_ROW;
    }

    @Override
    public List<ItemStack> getContents() {
        return this.stacks;
    }

    @Override
    public void onOpen(CraftHumanEntity player) {
        this.transaction.add(player);
    }

    @Override
    public void onClose(CraftHumanEntity player) {
        this.transaction.remove(player);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return this.transaction;
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStack;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public @Nullable InventoryHolder getOwner() {
        return null;
    }

    @Override
    public @Nullable Location getLocation() {
        return null;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < this.slots.size()) {
            return this.slots.get(slot).getItem();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        if (slot >= 0 && slot < this.slots.size()) {
            return this.slots.get(slot).remove(count);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slotObj = this.slots.get(slot);
        ItemStack stack = slotObj.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        slotObj.set(ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        Slot slotObj = this.slots.get(slot);
        slotObj.set(itemStack);
        itemStack.limitSize(this.getMaxStackSize(itemStack));
        this.setChanged();
    }

    @Override
    public void setChanged() {
        this.state.getShopVillager().markDirty();
    }

    @Override
    public int getContainerSize() {
        return STOCK_ROWS * SLOTS_PER_ROW;
    }

    @Override
    public boolean isEmpty() {
        for (Slot slot : this.slots) {
            if (!slot.getItem().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.state.isValid(player);
    }

    @Override
    public void clearContent() {
        for (Slot slot : this.slots) {
            slot.set(ItemStack.EMPTY);
        }
        this.setChanged();
    }

    public StockMenuState getState() {
        return this.state;
    }

    public List<Slot> getSlots() {
        return this.slots;
    }
}
