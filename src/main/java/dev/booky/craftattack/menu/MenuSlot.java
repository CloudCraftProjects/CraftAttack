package dev.booky.craftattack.menu;
// Created by booky10 in CraftAttack (00:11 27.10.2025)

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@NullMarked
public final class MenuSlot {

    private final Function<MenuContext, ItemStack> stack;
    private final @Nullable Function<MenuClickContext, MenuClickResult> clickHandler;

    public MenuSlot(ItemStack stack) {
        this(stack, null);
    }

    public MenuSlot(ItemStack stack, @Nullable Function<MenuClickContext, MenuClickResult> clickHandler) {
        this(__ -> stack, clickHandler);
    }

    public MenuSlot(Function<MenuContext, ItemStack> stack) {
        this(stack, null);
    }

    public MenuSlot(
            Function<MenuContext, ItemStack> stack,
            @Nullable Function<MenuClickContext, MenuClickResult> clickHandler
    ) {
        this.stack = stack;
        this.clickHandler = clickHandler;
    }

    public static MenuSlot createEmptySlot() {
        return createEmptySlot(null);
    }

    public static MenuSlot createEmptySlot(@Nullable Function<MenuClickContext, MenuClickResult> clickHandler) {
        return new MenuSlot(ItemStack.empty(), clickHandler);
    }

    public ItemStack getStack(MenuContext ctx) {
        return this.stack.apply(ctx);
    }

    public MenuClickResult handleClick(MenuClickContext ctx) {
        if (this.clickHandler != null) {
            return this.clickHandler.apply(ctx);
        }
        return MenuClickResult.NONE;
    }
}
