package dev.booky.craftattack.menu.impl;
// Created by booky10 in CraftAttack (01:04 27.10.2025)

import dev.booky.craftattack.menu.Menu;
import dev.booky.craftattack.menu.MenuSlot;
import dev.booky.craftattack.menu.context.MenuContext;
import dev.booky.craftattack.menu.context.MenuSlotsArrayContext;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class MenuHandler extends AbstractMenuHandler<Menu> {

    public MenuHandler(Menu menu, Player player) {
        super(menu, player);
    }

    @Override
    public void provideContent(MenuSlot[] slots) {
        MenuSlotsArrayContext slotsCtx = new MenuSlotsArrayContext(this.menu, this.player, this.inventory);
        this.menu.supplySlots(slotsCtx);

        @Nullable MenuSlot[] suppliedSlots = slotsCtx.getSlots();
        for (int i = 0; i < slots.length; i++) {
            MenuSlot slot = suppliedSlots[i];
            if (slot != null) {
                slots[i] = slot;
            }
        }
    }
}
