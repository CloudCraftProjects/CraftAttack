package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (20:18 27.10.2025)

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@NullMarked
public final class ItemStackUtil {

    private ItemStackUtil() {
    }

    public static ItemStack itemStack(Material material, Component name, Component... lore) {
        return itemStack(material, name, List.of(lore));
    }

    public static ItemStack itemStack(Material material, Component name, List<? extends ComponentLike> lore) {
        ItemStack stack = itemStack(material, name);
        ItemLore.Builder builder = ItemLore.lore();
        for (ComponentLike line : lore) {
            builder.addLine(text().decoration(ITALIC, false).append(line).build());
        }
        stack.setData(DataComponentTypes.LORE, builder.build());
        return stack;
    }

    public static ItemStack itemStack(Material material, Component name) {
        ItemStack stack = ItemStack.of(material);
        stack.setData(DataComponentTypes.RARITY, ItemRarity.COMMON);
        stack.setData(DataComponentTypes.ITEM_NAME, name);
        // force empty attribute modifier to prevent ugly lore from showing up
        stack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build());
        return stack;
    }
}
