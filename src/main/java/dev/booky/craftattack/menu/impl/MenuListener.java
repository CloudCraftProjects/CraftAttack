package dev.booky.craftattack.menu.impl;
// Created by booky10 in CraftAttack (00:05 27.10.2025)

import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MenuListener implements Listener {

    private final MenuManager manager;

    public MenuListener(MenuManager manager) {
        this.manager = manager;
    }
}
