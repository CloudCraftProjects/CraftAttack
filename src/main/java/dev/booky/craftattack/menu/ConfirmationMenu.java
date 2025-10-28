package dev.booky.craftattack.menu;
// Created by booky10 in CraftAttack (17:41 28.10.2025)

import dev.booky.craftattack.menu.context.MenuClickContext;
import dev.booky.craftattack.menu.context.MenuCloseContext;
import dev.booky.craftattack.menu.context.MenuContext;
import dev.booky.craftattack.menu.impl.AbstractMenuHandler;
import dev.booky.craftattack.menu.impl.ConfirmationMenuHandler;
import dev.booky.craftattack.menu.result.MenuClickResult;
import dev.booky.craftattack.menu.result.MenuResult;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@NullMarked
public class ConfirmationMenu extends AbstractMenu {

    private final @Nullable Function<MenuContext, Component> question;
    private final Function<MenuClickContext, MenuClickResult> confirmHandler;
    private final Function<MenuClickContext, MenuClickResult> cancelHandler;

    public ConfirmationMenu(
            Component title, int rows, boolean storage,
            Function<MenuCloseContext, MenuResult> closeHandler,
            Function<MenuClickContext, MenuClickResult> clickHandler,
            Function<MenuClickContext, MenuClickResult> backHandler,
            @Nullable Function<MenuContext, Component> question,
            Function<MenuClickContext, MenuClickResult> confirmHandler,
            Function<MenuClickContext, MenuClickResult> cancelHandler
    ) {
        super(title, rows, storage, closeHandler, clickHandler, backHandler);
        this.question = question;
        this.confirmHandler = confirmHandler;
        this.cancelHandler = cancelHandler;
    }

    public static ConfirmationMenuBuilder builder() {
        return new ConfirmationMenuBuilder();
    }

    @ApiStatus.Internal
    public @Nullable Component getQuestion(MenuContext ctx) {
        return this.question != null ? this.question.apply(ctx) : null;
    }

    @ApiStatus.Internal
    public MenuClickResult handleConfirm(MenuClickContext ctx) {
        return this.confirmHandler.apply(ctx);
    }

    @ApiStatus.Internal
    public MenuClickResult handleCancel(MenuClickContext ctx) {
        return this.cancelHandler.apply(ctx);
    }

    @ApiStatus.Internal
    @Override
    public AbstractMenuHandler<?> createHandler(Player player) {
        return new ConfirmationMenuHandler(this, player);
    }

    @Override
    public Builder<?, ?> copy() {
        ConfirmationMenuBuilder builder = new ConfirmationMenuBuilder();
        this.copy0(builder);
        return builder;
    }

    @Override
    protected void copy0(Builder<?, ?> builder) {
        super.copy0(builder);
        ((ConfirmationMenuBuilder) builder).question = this.question;
        ((ConfirmationMenuBuilder) builder).confirmHandler = this.confirmHandler;
        ((ConfirmationMenuBuilder) builder).cancelHandler = this.cancelHandler;
    }

    public static final class ConfirmationMenuBuilder extends AbstractMenu.Builder<ConfirmationMenuBuilder, ConfirmationMenu> {

        private static final Function<MenuClickContext, MenuClickResult> DEFAULT_HANDLER_NO_PARENT = __ -> MenuClickResult.CLOSE_SOUND;
        private static final Function<@Nullable AbstractMenu, Function<MenuClickContext, MenuClickResult>> DEFAULT_HANDLER = parent -> {
            if (parent == null) {
                return DEFAULT_HANDLER_NO_PARENT;
            }
            return ctx -> {
                parent.open(ctx.getPlayer());
                return MenuClickResult.SOUND;
            };
        };

        private @Nullable Function<MenuContext, Component> question;
        private Function<MenuClickContext, MenuClickResult> confirmHandler = DEFAULT_HANDLER_NO_PARENT;
        private Function<MenuClickContext, MenuClickResult> cancelHandler = DEFAULT_HANDLER_NO_PARENT;

        public ConfirmationMenuBuilder withQuestion(@Nullable Function<MenuContext, Component> question) {
            this.question = question;
            return this;
        }

        public ConfirmationMenuBuilder withConfirmHandler(Function<MenuClickContext, MenuClickResult> confirmHandler) {
            this.confirmHandler = confirmHandler;
            return this;
        }

        public ConfirmationMenuBuilder withCancelHandler(Function<MenuClickContext, MenuClickResult> cancelHandler) {
            this.cancelHandler = cancelHandler;
            return this;
        }

        @Override
        public ConfirmationMenuBuilder withParent(@Nullable AbstractMenu parent) {
            if (this.confirmHandler == DEFAULT_HANDLER_NO_PARENT) {
                this.confirmHandler = DEFAULT_HANDLER.apply(parent);
            }
            if (this.cancelHandler == DEFAULT_HANDLER_NO_PARENT) {
                this.cancelHandler = DEFAULT_HANDLER.apply(parent);
            }
            return super.withParent(parent);
        }

        @Override
        public ConfirmationMenu build() {
            return new ConfirmationMenu(this.title, this.rows, this.storage, this.closeHandler,
                    this.clickHandler, this.backHandler, this.question, this.confirmHandler, this.cancelHandler);
        }
    }
}
