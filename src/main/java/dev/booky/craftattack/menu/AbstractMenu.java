package dev.booky.craftattack.menu;
// Created by booky10 in CraftAttack (00:05 27.10.2025)

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@NullMarked
public abstract class AbstractMenu {

    public static final int MIN_ROWS = 1;
    public static final int MAX_ROWS = 6;
    public static final int SLOTS_PER_ROW = 9;

    private final Component title;
    private final int rows;

    private final Function<MenuCloseContext, MenuResult> closeHandler;
    private final Function<MenuClickContext, MenuClickResult> clickHandler;
    private final Function<MenuClickContext, MenuClickResult> backHandler;

    protected AbstractMenu(
            Component title, int rows,
            Function<MenuCloseContext, MenuResult> closeHandler,
            Function<MenuClickContext, MenuClickResult> clickHandler,
            Function<MenuClickContext, MenuClickResult> backHandler
    ) {
        this.title = title;
        this.rows = rows;
        this.closeHandler = closeHandler;
        this.clickHandler = clickHandler;
        this.backHandler = backHandler;
    }

    @ApiStatus.Internal
    public final MenuResult handleClose(MenuCloseContext ctx) {
        return this.closeHandler.apply(ctx);
    }

    @ApiStatus.Internal
    public final MenuClickResult handleClick(MenuClickContext ctx) {
        return this.clickHandler.apply(ctx);
    }

    @ApiStatus.Internal
    public final MenuClickResult handleBack(MenuClickContext ctx) {
        return this.backHandler.apply(ctx);
    }

    public void open(Player player) {
        // TODO
    }

    public void updateContent(Player player) {
        // TODO
    }

    public Component getTitle() {
        return this.title;
    }

    public int getRows() {
        return this.rows;
    }

    public int getSlots() {
        return this.rows * SLOTS_PER_ROW;
    }

    public abstract AbstractMenu.Builder<?, ?> copy();

    protected void copy0(AbstractMenu.Builder<?, ?> builder) {
        builder.title = this.title;
        builder.rows = this.rows;
        builder.closeHandler = this.closeHandler;
        builder.clickHandler = this.clickHandler;
        builder.backHandler = this.backHandler;
    }

    public static abstract class Builder<B extends Builder<B, T>, T extends AbstractMenu> {

        private static final Function<MenuCloseContext, MenuResult> DEFAULT_CLOSE_HANDLER = ctx -> MenuResult.NONE;
        private static final Function<MenuClickContext, MenuClickResult> DEFAULT_CLICK_HANDLER = ctx -> MenuClickResult.NONE;
        private static final Function<MenuClickContext, MenuClickResult> DEFAULT_BACK_HANDLER = ctx -> MenuClickResult.CLOSE_SOUND;

        protected Component title = Component.empty();
        protected int rows = 3;

        protected Function<MenuCloseContext, MenuResult> closeHandler = DEFAULT_CLOSE_HANDLER;
        protected Function<MenuClickContext, MenuClickResult> clickHandler = DEFAULT_CLICK_HANDLER;
        protected Function<MenuClickContext, MenuClickResult> backHandler = DEFAULT_BACK_HANDLER;

        protected Builder() {
        }

        @SuppressWarnings("unchecked")
        protected B getSelf() {
            return (B) this;
        }

        public B withTitle(Component title) {
            this.title = title;
            return this.getSelf();
        }

        public B withSlots(int slots) {
            int rows = NumberConversions.ceil(slots / (double) SLOTS_PER_ROW);
            int clampedRows = Math.clamp(rows, MIN_ROWS, MAX_ROWS);
            return this.withRows(clampedRows);
        }

        public B withRows(int rows) {
            if (rows < MIN_ROWS || rows > MAX_ROWS) {
                throw new IllegalArgumentException("Out-of-bounds row count received: " + rows);
            }
            this.rows = rows;
            return this.getSelf();
        }

        public B withParent(@Nullable AbstractMenu parent) {
            // don't discard existing back handler
            Function<MenuClickContext, MenuClickResult> handler = this.backHandler;
            return this.onBack(ctx -> {
                MenuClickResult ret = handler.apply(ctx);
                if (parent != null) {
                    parent.open(ctx.getPlayer());
                }
                return ret;
            });
        }

        public B onClose(Function<MenuCloseContext, MenuResult> handler) {
            this.closeHandler = handler;
            return this.getSelf();
        }

        public B onClick(Function<MenuClickContext, MenuClickResult> handler) {
            this.clickHandler = handler;
            return this.getSelf();
        }

        public B onBack(Function<MenuClickContext, MenuClickResult> handler) {
            this.backHandler = handler;
            return this.getSelf();
        }

        public void open(Player player) {
            this.build().open(player);
        }

        public abstract T build();
    }
}
