package dev.booky.craftattack.menu;
// Created by booky10 in CraftAttack (00:06 27.10.2025)

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.function.Consumer;
import java.util.function.Function;

@NullMarked
public final class Menu extends AbstractMenu {

    private final Consumer<MenuSlotsArrayContext> slots;

    private Menu(
            Component title, int rows,
            Function<MenuCloseContext, MenuResult> closeHandler,
            Function<MenuClickContext, MenuClickResult> clickHandler,
            Function<MenuClickContext, MenuClickResult> backHandler,
            Consumer<MenuSlotsArrayContext> slots
    ) {
        super(title, rows, closeHandler, clickHandler, backHandler);
        this.slots = slots;
    }

    public static MenuBuilder builder() {
        return new MenuBuilder();
    }

    @ApiStatus.Internal
    public void supplySlots(MenuSlotsArrayContext ctx) {
        this.slots.accept(ctx);
    }

    @Override
    public MenuBuilder copy() {
        MenuBuilder builder = new MenuBuilder();
        this.copy0(builder);
        return builder;
    }

    @Override
    protected void copy0(Builder<?, ?> builder) {
        super.copy0(builder);
        ((MenuBuilder) builder).slots = this.slots;
    }

    public static final class MenuBuilder extends AbstractMenu.Builder<MenuBuilder, Menu> {

        private static final Consumer<MenuSlotsArrayContext> DEFAULT_CONTENT = ctx -> {/**/};
        private Consumer<MenuSlotsArrayContext> slots = DEFAULT_CONTENT;

        public MenuBuilder withSlots(Consumer<MenuSlotsArrayContext> slots) {
            this.slots = slots;
            return this;
        }

        @Override
        public Menu build() {
            return new Menu(this.title, this.rows, this.closeHandler, this.clickHandler, this.backHandler, this.slots);
        }
    }
}
