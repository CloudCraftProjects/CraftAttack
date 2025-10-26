package dev.booky.craftattack.menu;
// Created by booky10 in CraftAttack (00:07 27.10.2025)

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class MenuContext {

    protected final AbstractMenu menu;
    protected final Player player;
    protected final Inventory inventory;
    protected final InventoryView view;

    @ApiStatus.Internal
    public MenuContext(AbstractMenu menu, Player player, Inventory inventory, InventoryView view) {
        this.menu = menu;
        this.player = player;
        this.inventory = inventory;
        this.view = view;
    }

    public AbstractMenu getMenu() {
        return this.menu;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public InventoryView getView() {
        return this.view;
    }
}
