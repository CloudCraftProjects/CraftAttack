package dev.booky.craftattack.menu.impl;
// Created by booky10 in CraftAttack (00:43 27.10.2025)

import dev.booky.craftattack.menu.MenuSlot;
import dev.booky.craftattack.menu.PagedMenu;
import dev.booky.craftattack.menu.context.MenuSlotsListContext;
import dev.booky.craftattack.menu.result.MenuClickResult;
import dev.booky.craftattack.utils.PlayerHeadUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.List;

import static dev.booky.craftattack.menu.AbstractMenu.SLOTS_PER_ROW;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@NullMarked
public class PagedMenuHandler extends AbstractMenuHandler<PagedMenu> {

    private MenuSlot @MonotonicNonNull [] content;
    private int pages;
    private int page = 0;

    public PagedMenuHandler(MenuManager manager, PagedMenu menu, Player player) {
        super(manager, menu, player);
    }

    @Override
    public void refreshContent() {
        MenuSlotsListContext ctx = new MenuSlotsListContext(this.manager, this.menu, this.player, this.inventory);
        this.menu.supplySlots(ctx);

        List<MenuSlot> content = ctx.getSlots();
        this.content = content.toArray(new MenuSlot[0]);

        // this next button isn't always present, so we have to account for this
        int contentSize = this.inventorySize - 2;
        int pageCount = NumberConversions.ceil(content.size() / (double) contentSize);
        if (pageCount > 1) {
            // check whether the content on the last page would fit perfectly if we remove the next button
            if (content.size() % contentSize == 1) {
                // we would have a single slot on the next page; we can
                // prevent this, just do one page less than calculated
                pageCount--;
            }
        }
        this.pages = Math.max(1, pageCount); // at least 1 page is required

        // limit current page index if the number of pages decreased during a content update
        this.page = Math.min(this.page, this.pages - 1);

        super.refreshContent();
    }

    @Override
    public void updateInventory() {
        this.currentSlots = this.provideContent();
        super.updateInventory();
    }

    @Override
    public void provideContent(MenuSlot[] slots) {
        if (this.content.length == 0) {
            return; // no content provided
        }
        int contentOffset = this.page * (this.inventorySize - 2);
        int remainingItems = this.content.length - contentOffset;
        // copy over top part
        int fullSlots = this.inventorySize - SLOTS_PER_ROW;
        if (fullSlots > 0) {
            int copyCount = Math.min(remainingItems, fullSlots);
            System.arraycopy(this.content, contentOffset, slots, 0, copyCount);
            contentOffset += copyCount;
            remainingItems -= copyCount;
        }
        if (remainingItems > 0) {
            // insert bottom part between controls
            int copyCount = remainingItems == SLOTS_PER_ROW - 1 ? remainingItems : Math.min(remainingItems, SLOTS_PER_ROW - 2);
            System.arraycopy(this.content, contentOffset, slots,
                    this.inventorySize - SLOTS_PER_ROW + 1, copyCount);
        }
    }

    @Override
    public void setupControls(MenuSlot[] slots) {
        super.setupControls(slots);
        if (this.page > 0) {
            slots[this.inventorySize - SLOTS_PER_ROW] = this.providePreviousPage();
        }
        if (this.page < this.pages - 1) {
            slots[this.inventorySize - 1] = this.provideNextPage();
        }
    }

    private MenuSlot providePreviousPage() {
        ItemStack stack = PlayerHeadUtil.createHeadStack(PlayerHeadUtil.WHITE_ARROW_LEFT_TEXTURE);
        Component name = translatable("ca.menu.previous", text(this.page));
        stack.setData(DataComponentTypes.CUSTOM_NAME, name);
        return new MenuSlot(stack, ctx -> {
            this.updatePage(-1);
            return MenuClickResult.SOUND;
        });
    }

    private MenuSlot provideNextPage() {
        ItemStack stack = PlayerHeadUtil.createHeadStack(PlayerHeadUtil.WHITE_ARROW_RIGHT_TEXTURE);
        Component name = translatable("ca.menu.next", text(this.page + 1 + 1));
        stack.setData(DataComponentTypes.CUSTOM_NAME, name);
        return new MenuSlot(stack, ctx -> {
            this.updatePage(1);
            return MenuClickResult.SOUND;
        });
    }

    public void updatePage(int offset) {
        int newPage = Math.clamp(this.page + offset, 0, this.pages - 1);
        if (this.page != newPage) {
            this.page = newPage;
            this.updateInventory();
        }
    }
}
