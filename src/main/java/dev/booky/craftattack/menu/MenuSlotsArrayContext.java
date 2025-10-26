package dev.booky.craftattack.menu;
// Created by booky10 in CraftAttack (00:29 27.10.2025)

import com.google.common.collect.Iterators;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NullMarked;

import java.util.Iterator;

import static dev.booky.craftattack.menu.AbstractMenu.SLOTS_PER_ROW;

@NullMarked
public final class MenuSlotsArrayContext extends MenuContext implements Iterable<MenuSlot> {

    private final MenuSlot[] slots;

    public MenuSlotsArrayContext(AbstractMenu menu, Player player, Inventory inventory, InventoryView view) {
        super(menu, player, inventory, view);
        this.slots = new MenuSlot[menu.getRows() * SLOTS_PER_ROW];
    }

    public MenuSlot[] getSlots() {
        return this.slots;
    }

    public int size() {
        return this.slots.length;
    }

    @Override
    public Iterator<MenuSlot> iterator() {
        return Iterators.forArray(this.slots);
    }
}
