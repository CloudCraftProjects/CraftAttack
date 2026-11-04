package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (12:20 PM 04.11.2026)

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class StockMenuContentSlot extends Slot {

    private final StockMenuState state;

    public StockMenuContentSlot(Container container, int slot, StockMenuState state) {
        super(container, slot, 0, 0);
        this.state = state;
    }

    @Override
    public ItemStack getItem() {
        this.state.lock();
        try {
            return this.state.getStack(this.getContainerSlot());
        } finally {
            this.state.unlock();
        }
    }

    @Override
    public void set(ItemStack stack) {
        this.state.lock();
        try {
            this.state.setStack(this.getContainerSlot(), stack);
            this.setChanged();
        } finally {
            this.state.unlock();
        }
    }

    @Override
    public ItemStack remove(int amount) {
        this.state.lock();
        try {
            int slot = this.getContainerSlot();
            ItemStack stack = this.state.getStack(slot);
            if (!stack.isEmpty() && amount > 0) {
                return stack.split(amount);
            }
            return ItemStack.EMPTY;
        } finally {
            this.state.unlock();
        }
    }
}
