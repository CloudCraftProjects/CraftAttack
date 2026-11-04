package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (3:07 PM 04.11.2026)

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
class StockChestMenu extends ChestMenu {

    public StockChestMenu(int containerId, Inventory inventory, StockMenuContainer container) {
        super(MenuType.GENERIC_9x6, containerId, inventory, container, ShopMenu.STOCK_ROWS);
        // override slots
        List<Slot> customSlots = container.getSlots();
        for (int i = 0; i < customSlots.size(); i++) {
            Slot slot = customSlots.get(i);
            slot.index = i;
            this.slots.set(i, slot);
        }
        this.setTitle(Component.translatable("ca.menu.shop.manage-stock"));
    }
}
