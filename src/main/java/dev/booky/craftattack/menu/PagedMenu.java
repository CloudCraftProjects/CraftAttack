package dev.booky.craftattack.menu;
// Created by booky10 in CraftAttack (00:06 27.10.2025)

import dev.booky.craftattack.menu.context.MenuClickContext;
import dev.booky.craftattack.menu.context.MenuCloseContext;
import dev.booky.craftattack.menu.context.MenuSlotsListContext;
import dev.booky.craftattack.menu.impl.AbstractMenuHandler;
import dev.booky.craftattack.menu.impl.PagedMenuHandler;
import dev.booky.craftattack.menu.result.MenuClickResult;
import dev.booky.craftattack.menu.result.MenuResult;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.function.Consumer;
import java.util.function.Function;

@NullMarked
public final class PagedMenu extends AbstractMenu {

    private final Consumer<MenuSlotsListContext> slots;

    private PagedMenu(
            Component title, int rows,
            Function<MenuCloseContext, MenuResult> closeHandler,
            Function<MenuClickContext, MenuClickResult> clickHandler,
            Function<MenuClickContext, MenuClickResult> backHandler,
            Consumer<MenuSlotsListContext> slots
    ) {
        super(title, rows, closeHandler, clickHandler, backHandler);
        this.slots = slots;
    }

    public static PagedMenuBuilder builder() {
        return new PagedMenuBuilder();
    }

    @ApiStatus.Internal
    public void supplySlots(MenuSlotsListContext ctx) {
        this.slots.accept(ctx);
    }

    @ApiStatus.Internal
    @Override
    public AbstractMenuHandler<?> createHandler(Player player) {
        return new PagedMenuHandler(this, player);
    }

    @Override
    public PagedMenuBuilder copy() {
        PagedMenuBuilder builder = new PagedMenuBuilder();
        this.copy0(builder);
        return builder;
    }

    @Override
    protected void copy0(Builder<?, ?> builder) {
        super.copy0(builder);
        ((PagedMenuBuilder) builder).slots = this.slots;
    }

    public static final class PagedMenuBuilder extends Builder<PagedMenuBuilder, PagedMenu> {

        private static final Consumer<MenuSlotsListContext> DEFAULT_CONTENT = ctx -> {/**/};
        private Consumer<MenuSlotsListContext> slots = DEFAULT_CONTENT;

        public PagedMenuBuilder withSlots(Consumer<MenuSlotsListContext> slots) {
            this.slots = slots;
            return this;
        }

        @Override
        public PagedMenu build() {
            return new PagedMenu(this.title, this.rows, this.closeHandler, this.clickHandler, this.backHandler, this.slots);
        }
    }
}
