package dev.booky.craftattack.menu.impl;
// Created by booky10 in CraftAttack (00:43 27.10.2025)

import dev.booky.craftattack.menu.MenuSlot;
import dev.booky.craftattack.menu.PagedMenu;
import dev.booky.craftattack.menu.context.MenuSlotsListContext;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class PagedMenuHandler extends AbstractMenuHandler<PagedMenu> {

    private MenuSlot[] allSlots;
    private int pages;
    private int page = 0;

    public PagedMenuHandler(PagedMenu menu, Player player) {
        super(menu, player);
    }

    @Override
    public void refreshContent() {
        MenuSlotsListContext ctx = new MenuSlotsListContext(this.menu, this.player, this.inventory);
        this.menu.supplySlots(ctx);
        List<MenuSlot> allSlots = ctx.getSlots();

        int contentSize = this.inventorySize - 1;
        // TODO

        super.refreshContent();
    }

    @Override
    public void updateInventory() {
        super.refreshContent();
    }

    @Override
    public void provideContent(MenuSlot[] slots) {

    }
}
