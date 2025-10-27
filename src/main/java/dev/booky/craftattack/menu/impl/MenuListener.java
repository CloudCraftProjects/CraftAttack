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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public class MenuListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder(false) instanceof AbstractMenuHandler<?> handler)) {
            return; // not our problem
        }

        boolean cancel = false;
        boolean handle = event.getSlotType() == InventoryType.SlotType.CONTAINER;

        if (event.getClick() == ClickType.DOUBLE_CLICK) {
            // double clicks may move items from the top inventory to the cursor
            cancel = true;
            // don't handle double clicks, they occur at the same time as other types of clicks
            handle = false;
        }
        if (!cancel && (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
            // always cancel shift clicks, prevent players from shifting items in/out of menus
            cancel = true;
        }
        if (handle && event.getClickedInventory() != event.getInventory()) {
            handle = false; // don't handle clicks in the players inventory
        }

        if (handle) {
            ItemStack stack = event.getInventory().getItem(event.getSlot());
            stack = Objects.requireNonNullElseGet(stack, ItemStack::empty);

            MenuClickResult result = handler.handleClick(event.getView(), event.getRawSlot(), stack, event.getClick());
            if (!result.isAllow()) {
                cancel = true;
            }
            HumanEntity clicker = event.getWhoClicked();
            Sound sound = result.getSound();
            if (sound != null) {
                clicker.playSound(sound, clicker);
            }
            // only close if we are still managing this inventory
            if (result.isClose() && event.getView() == clicker.getOpenInventory()) {
                clicker.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            }
        }

        if (cancel) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder(false) instanceof AbstractMenuHandler<?> handler)) {
            return; // not our problem
        }

        MenuResult result = handler.handleClose(event.getView(), event.getReason());
        Sound sound = result.getSound();
        if (sound != null) {
            HumanEntity clicker = event.getPlayer();
            clicker.playSound(sound, clicker);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder(false) instanceof AbstractMenuHandler<?>)) {
            return; // not our problem
        }

        // if any of the dragged slots are in the top inventory,
        // cancel the entire quickcraft process, I don't want to deal with this right now
        InventoryView view = event.getView();
        Inventory topInv = view.getTopInventory();
        for (int slot : event.getRawSlots()) {
            if (view.getInventory(slot) == topInv) {
                event.setCancelled(true);
                break;
            }
        }
    }
}
