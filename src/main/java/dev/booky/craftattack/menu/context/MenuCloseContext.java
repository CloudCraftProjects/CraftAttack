package dev.booky.craftattack.menu.context;
// Created by booky10 in CraftAttack (00:08 27.10.2025)

import dev.booky.craftattack.menu.AbstractMenu;
import dev.booky.craftattack.menu.impl.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MenuCloseContext extends MenuContext {

    private final InventoryView view;
    private final InventoryCloseEvent.Reason reason;

    @ApiStatus.Internal
    public MenuCloseContext(MenuManager manager, AbstractMenu menu, Player player, Inventory inventory, InventoryView view, InventoryCloseEvent.Reason reason) {
        super(manager, menu, player, inventory);
        this.view = view;
        this.reason = reason;
    }

    public InventoryView getView() {
        return this.view;
    }

    public InventoryCloseEvent.Reason getReason() {
        return this.reason;
    }
}
