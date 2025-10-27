package dev.booky.craftattack.menu.context;
// Created by booky10 in CraftAttack (00:07 27.10.2025)

import dev.booky.craftattack.menu.AbstractMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MenuContext {

    protected final AbstractMenu menu;
    protected final Player player;
    protected final Inventory inventory;

    @ApiStatus.Internal
    public MenuContext(AbstractMenu menu, Player player, Inventory inventory) {
        this.menu = menu;
        this.player = player;
        this.inventory = inventory;
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
}
