package dev.booky.craftattack.menu.impl;
// Created by booky10 in CraftAttack (00:41 27.10.2025)

import dev.booky.craftattack.menu.AbstractMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.WeakHashMap;

@NullMarked
public final class MenuManager {

    private final Map<Player, AbstractMenuHandler<?>> menus = new WeakHashMap<>();

    public void open(AbstractMenu menu, Player player) {
        AbstractMenuHandler<?> handler = menu.createHandler(this, player);
        handler.inventory = Bukkit.createInventory(handler, menu.getSlots(), menu.getTitle());

        // initialize initial inventory content and open inventory
        handler.refreshContent();
        player.openInventory(handler.inventory);
        // after inventory has been opened, save handler in map
        // (we need to wait until after the inventory has been opened because of close events)
        this.menus.put(player, handler);
    }

    public void updateContent(AbstractMenu menu, Player player) {
        AbstractMenuHandler<?> handler = this.menus.get(player);
        if (handler != null && handler.menu == menu) {
            handler.refreshContent();
        }
    }
}
