package dev.booky.craftattack.menu;
// Created by booky10 in CraftAttack (00:08 27.10.2025)

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MenuCloseContext extends MenuContext {

    private final InventoryCloseEvent.Reason reason;

    @ApiStatus.Internal
    public MenuCloseContext(AbstractMenu menu, Player player, Inventory inventory, InventoryView view, InventoryCloseEvent.Reason reason) {
        super(menu, player, inventory, view);
        this.reason = reason;
    }

    public InventoryCloseEvent.Reason getReason() {
        return this.reason;
    }
}
