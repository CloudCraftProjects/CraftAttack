package dev.booky.craftattack.menu.context;
// Created by booky10 in CraftAttack (00:29 27.10.2025)

import com.google.common.collect.Iterators;
import dev.booky.craftattack.menu.AbstractMenu;
import dev.booky.craftattack.menu.MenuSlot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

import static dev.booky.craftattack.menu.AbstractMenu.SLOTS_PER_ROW;

@NullMarked
public final class MenuSlotsArrayContext extends MenuContext implements Iterable<@Nullable MenuSlot> {

    private final @Nullable MenuSlot[] slots;

    public MenuSlotsArrayContext(AbstractMenu menu, Player player, Inventory inventory) {
        super(menu, player, inventory);
        this.slots = new MenuSlot[menu.getRows() * SLOTS_PER_ROW];
    }

    public @Nullable MenuSlot[] getSlots() {
        return this.slots;
    }

    public int size() {
        return this.slots.length;
    }

    @Override
    public Iterator<@Nullable MenuSlot> iterator() {
        return Iterators.forArray(this.slots);
    }
}
