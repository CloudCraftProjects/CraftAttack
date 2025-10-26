package dev.booky.craftattack.menu;
// Created by booky10 in CraftAttack (00:09 27.10.2025)

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MenuClickContext extends MenuContext {

    private final int rawSlot;
    private final int slot;
    private final ItemStack stack;
    private final ItemStack cursor;
    private final ClickType clickType;

    @ApiStatus.Internal
    public MenuClickContext(AbstractMenu menu, Player player, Inventory inventory, InventoryView view, int rawSlot, int slot, ItemStack stack, ItemStack cursor, ClickType clickType) {
        super(menu, player, inventory, view);
        this.rawSlot = rawSlot;
        this.slot = slot;
        this.stack = stack;
        this.cursor = cursor;
        this.clickType = clickType;
    }

    public int getRawSlot() {
        return this.rawSlot;
    }

    public int getSlot() {
        return this.slot;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public ItemStack getCursor() {
        return this.cursor;
    }

    public ClickType getClickType() {
        return this.clickType;
    }
}
