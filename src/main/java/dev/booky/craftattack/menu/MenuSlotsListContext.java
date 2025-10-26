package dev.booky.craftattack.menu;
// Created by booky10 in CraftAttack (00:29 27.10.2025)

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static dev.booky.craftattack.menu.AbstractMenu.SLOTS_PER_ROW;

@NullMarked
public final class MenuSlotsListContext extends MenuContext implements Iterable<MenuSlot> {

    private final List<MenuSlot> slots = new ArrayList<>(4 * SLOTS_PER_ROW);

    public MenuSlotsListContext(AbstractMenu menu, Player player, Inventory inventory, InventoryView view) {
        super(menu, player, inventory, view);
    }

    public void add(MenuSlot slot) {
        this.slots.add(slot);
    }

    public void add(MenuSlot... slots) {
        if (slots.length > 0) {
            this.slots.addAll(List.of(slots));
        }
    }

    public void add(Collection<MenuSlot> slots) {
        this.slots.addAll(slots);
    }

    public List<MenuSlot> getSlots() {
        return this.slots;
    }

    public int size() {
        return this.slots.size();
    }

    @Override
    public Iterator<MenuSlot> iterator() {
        return this.slots.iterator();
    }
}
