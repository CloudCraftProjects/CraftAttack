package dev.booky.craftattack.menu.impl;
// Created by booky10 in CraftAttack (00:05 27.10.2025)

import dev.booky.craftattack.menu.result.MenuClickResult;
import dev.booky.craftattack.menu.result.MenuResult;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public class MenuListener implements Listener {

    private final MenuManager manager;

    public MenuListener(MenuManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        AbstractMenuHandler<?> handler = this.manager.getHandler(clicker.getUniqueId());
        if (handler == null) {
            return; // not our problem
        }

        // TODO revisit these checks, they don't seem safe
        if (event.getSlotType() == InventoryType.SlotType.CONTAINER
                && event.getClickedInventory() == event.getInventory()
                && event.getClick() != ClickType.DOUBLE_CLICK
                && event.getClickedInventory() == event.getView().getTopInventory()) {
            ItemStack stack = event.getInventory().getItem(event.getSlot());
            stack = Objects.requireNonNullElseGet(stack, ItemStack::empty);

            MenuClickResult result = handler.handleClick(event.getView(), event.getRawSlot(), stack, event.getClick());
            if (!result.isAllow()) {
                event.setCancelled(true);
            }
            Sound sound = result.getSound();
            if (sound != null) {
                clicker.playSound(sound, clicker);
            }
            // only close if we are still managing this inventory
            if (result.isClose() && handler == this.manager.getHandler(clicker.getUniqueId())) {
                clicker.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            }
        }

        // TODO what the fuck are these checks?
        if (!event.isCancelled() && event.getRawSlot() != InventoryView.OUTSIDE && event.getRawSlot() != -1) {
            if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                // cancel all clicks targeting the top inventory // TODO WHY
                event.setCancelled(true);
            } else if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                // always cancel shift clicks // TODO WHY
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity clicker = event.getPlayer();
        AbstractMenuHandler<?> handler = this.manager.clearHandler(clicker.getUniqueId());
        if (handler == null) {
            return; // not our problem
        }

        MenuResult result = handler.handleClose(event.getView(), event.getReason());
        Sound sound = result.getSound();
        if (sound != null) {
            clicker.playSound(sound, clicker);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        AbstractMenuHandler<?> handler = this.manager.clearHandler(clicker.getUniqueId());
        if (handler == null) {
            return; // not our problem
        }

        // if any of the dragged slots are in the top inventory,
        // cancel the entire quickcraft process, I don't want to deal with this right now
        int inventorySize = event.getView().getTopInventory().getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < inventorySize) {
                event.setCancelled(true);
                break;
            }
        }
    }
}
