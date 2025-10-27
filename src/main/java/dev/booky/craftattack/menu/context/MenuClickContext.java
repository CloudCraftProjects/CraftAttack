package dev.booky.craftattack.menu.context;
// Created by booky10 in CraftAttack (00:09 27.10.2025)

import dev.booky.craftattack.menu.AbstractMenu;
import dev.booky.craftattack.menu.impl.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MenuClickContext extends MenuContext {

    private final InventoryView view;
    private final int rawSlot;
    private final int slot;
    private final ItemStack stack;
    private final ItemStack cursor;
    private final ClickType clickType;

    @ApiStatus.Internal
    public MenuClickContext(MenuManager manager, AbstractMenu menu, Player player, Inventory inventory, InventoryView view, int rawSlot, int slot, ItemStack stack, ItemStack cursor, ClickType clickType) {
        super(manager, menu, player, inventory);
        this.view = view;
        this.rawSlot = rawSlot;
        this.slot = slot;
        this.stack = stack;
        this.cursor = cursor;
        this.clickType = clickType;
    }

    public InventoryView getView() {
        return this.view;
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
