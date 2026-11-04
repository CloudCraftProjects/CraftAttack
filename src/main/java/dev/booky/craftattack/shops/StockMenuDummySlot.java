package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (1:06 PM 04.11.2026)

import net.minecraft.world.Container;
import net.minecraft.world.inventory.NonInteractiveResultSlot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StockMenuDummySlot extends NonInteractiveResultSlot {

    public StockMenuDummySlot(Container container, int slot) {
        super(container, slot, 0, 0);
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void set(ItemStack itemStack) {
    }
}
