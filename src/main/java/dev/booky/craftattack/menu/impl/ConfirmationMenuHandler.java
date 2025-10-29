package dev.booky.craftattack.menu.impl;
// Created by booky10 in CraftAttack (17:51 28.10.2025)

import dev.booky.craftattack.menu.ConfirmationMenu;
import dev.booky.craftattack.menu.MenuSlot;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import static dev.booky.craftattack.menu.AbstractMenu.SLOTS_PER_ROW;
import static dev.booky.craftattack.utils.ItemStackUtil.itemStack;
import static net.kyori.adventure.text.Component.translatable;

@NullMarked
public class ConfirmationMenuHandler extends AbstractMenuHandler<ConfirmationMenu> {

    public ConfirmationMenuHandler(ConfirmationMenu menu, Player player) {
        super(menu, player);
    }

    @Override
    public void provideContent(MenuSlot[] slots) {
        this.setupControls(slots);

        // center vertically
        int centerRow = this.menu.getRows() / 2;
        int slotOffset = centerRow * SLOTS_PER_ROW;

        // 00C0Q0C00
        slots[slotOffset + SLOTS_PER_ROW / 2 - 2] = new MenuSlot(
                itemStack(Material.LIME_DYE, translatable("ca.menu.confirm.confirm")),
                this.menu::handleConfirm);
        slots[slotOffset + SLOTS_PER_ROW / 2 + 2] = new MenuSlot(
                itemStack(Material.RED_DYE, translatable("ca.menu.confirm.cancel")),
                this.menu::handleCancel);

        Component question = this.menu.getQuestion(this.ctx);
        if (question != null) {
            ItemStack questionStack = itemStack(Material.PAPER, question);
            slots[slotOffset + SLOTS_PER_ROW / 2] = new MenuSlot(questionStack);
        }
    }
}
