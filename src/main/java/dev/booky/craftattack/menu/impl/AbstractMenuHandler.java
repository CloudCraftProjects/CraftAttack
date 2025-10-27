package dev.booky.craftattack.menu.impl;
// Created by booky10 in CraftAttack (00:43 27.10.2025)

import dev.booky.craftattack.menu.AbstractMenu;
import dev.booky.craftattack.menu.MenuSlot;
import dev.booky.craftattack.menu.context.MenuClickContext;
import dev.booky.craftattack.menu.context.MenuCloseContext;
import dev.booky.craftattack.menu.context.MenuContext;
import dev.booky.craftattack.menu.result.MenuClickResult;
import dev.booky.craftattack.menu.result.MenuResult;
import dev.booky.craftattack.utils.PlayerHeadUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

import static dev.booky.craftattack.menu.AbstractMenu.SLOTS_PER_ROW;

@NullMarked
public abstract class AbstractMenuHandler<T extends AbstractMenu> implements InventoryHolder {

    protected final T menu;
    protected final Player player;
    protected final Inventory inventory;
    protected final MenuContext ctx;

    protected final int inventorySize;
    protected MenuSlot @Nullable [] currentSlots;

    public AbstractMenuHandler(T menu, Player player) {
        this.menu = menu;
        this.player = player;
        this.inventorySize = menu.getRows() * 9;

        this.inventory = Bukkit.createInventory(this, menu.getSlots(), menu.getTitle());
        this.ctx = new MenuContext(this.menu, this.player, this.inventory);

        // set initial content in inventory
        this.refreshContent();
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void refreshContent() {
        this.currentSlots = this.provideContent();
        this.updateInventory();
    }

    public void updateInventory() {
        MenuSlot[] slots = this.currentSlots;
        if (slots != null) {
            for (int i = 0; i < slots.length; i++) {
                this.inventory.setItem(i, slots[i].getStack(this.ctx));
            }
        }
    }

    public final MenuSlot[] provideContent() {
        MenuSlot[] slots = new MenuSlot[this.inventorySize];
        Arrays.fill(slots, MenuSlot.createEmptySlot());
        this.provideContent(slots); // fill the actual content
        return slots;
    }

    public abstract void provideContent(MenuSlot[] slots);

    public void setupControls(MenuSlot[] slots) {
        slots[this.inventorySize - SLOTS_PER_ROW] = this.provideBack();
    }

    public MenuSlot provideBack() {
        ItemStack stack = PlayerHeadUtil.createHeadStack(PlayerHeadUtil.WHITE_ARROW_LEFT_TEXTURE);
        stack.setData(DataComponentTypes.CUSTOM_NAME, Component.translatable("ca.menu.back"));
        return new MenuSlot(stack, this.menu::handleBack);
    }

    public MenuClickResult handleClick(InventoryView view, int rawSlot, ItemStack stack, ClickType clickType) {
        int slot = view.convertSlot(rawSlot);
        MenuClickContext ctx = new MenuClickContext(this.menu, this.player, this.inventory, view,
                rawSlot, slot, stack, view.getCursor(), clickType);

        MenuClickResult result = MenuClickResult.NONE;
        if (this.currentSlots != null) {
            result = result.plus(this.currentSlots[slot].handleClick(ctx));
        }
        return result.plus(this.menu.handleClick(ctx));
    }

    public MenuResult handleClose(InventoryView view, InventoryCloseEvent.Reason reason) {
        return this.menu.handleClose(new MenuCloseContext(
                this.menu, this.player, this.inventory, view, reason));
    }

    public T getMenu() {
        return this.menu;
    }

    public Player getPlayer() {
        return this.player;
    }
}
