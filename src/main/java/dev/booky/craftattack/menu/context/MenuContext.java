package dev.booky.craftattack.menu.context;
// Created by booky10 in CraftAttack (00:07 27.10.2025)

import dev.booky.craftattack.menu.AbstractMenu;
import dev.booky.craftattack.menu.impl.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MenuContext {

    protected final MenuManager manager;
    protected final AbstractMenu menu;
    protected final Player player;
    protected final Inventory inventory;

    @ApiStatus.Internal
    public MenuContext(MenuManager manager, AbstractMenu menu, Player player, Inventory inventory) {
        this.manager = manager;
        this.menu = menu;
        this.player = player;
        this.inventory = inventory;
    }

    public MenuManager getManager() {
        return this.manager;
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
