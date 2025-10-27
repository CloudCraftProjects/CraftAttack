package dev.booky.craftattack.menu.impl;
// Created by booky10 in CraftAttack (00:43 27.10.2025)

import dev.booky.craftattack.menu.AbstractMenu;
import dev.booky.craftattack.menu.MenuSlot;
import dev.booky.craftattack.menu.context.MenuContext;
import dev.booky.craftattack.utils.PlayerHeadUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

import static dev.booky.craftattack.menu.AbstractMenu.SLOTS_PER_ROW;

@NullMarked
public abstract class AbstractMenuHandler<T extends AbstractMenu> implements InventoryHolder {

    protected final T menu;
    protected final Player player;

    // set after creation
    protected @MonotonicNonNull Inventory inventory;

    protected final int inventorySize;
    protected MenuSlot @Nullable [] currentSlots;

    public AbstractMenuHandler(T menu, Player player) {
        this.menu = menu;
        this.player = player;
        this.inventorySize = menu.getRows() * 9;
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
            MenuContext ctx = new MenuContext(this.menu, this.player, this.inventory);
            for (int i = 0; i < slots.length; i++) {
                this.inventory.setItem(i, slots[i].getStack(ctx));
            }
        }
    }

    public final MenuSlot[] provideContent() {
        MenuSlot[] slots = new MenuSlot[this.inventorySize];
        Arrays.fill(slots, MenuSlot.createEmptySlot());
        this.setupControls(slots); // setup previous/next/back buttons
        this.provideContent(slots); // fill the actual content
        return slots;
    }

    public abstract void provideContent(MenuSlot[] slots);

    public void setupControls(MenuSlot[] slots) {
        slots[this.inventorySize - SLOTS_PER_ROW + 1] = this.provideBack();
    }

    public MenuSlot provideBack() {
        ItemStack stack = PlayerHeadUtil.createHeadStack(PlayerHeadUtil.WHITE_ARROW_LEFT_TEXTURE);
        stack.setData(DataComponentTypes.CUSTOM_NAME, Component.translatable("ca.menu.back"));
        return new MenuSlot(stack, this.menu::handleBack);
    }
}
