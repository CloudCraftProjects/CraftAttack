package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (20:47 28.10.2025)

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ShopRecipes {

    private ShopRecipes() {
    }

    public static void registerRecipe(Plugin plugin) {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "shop/recipe");
        Bukkit.addRecipe(new ShapedRecipe(recipeKey, ShopVillager.createSpawnEgg(plugin))
                .shape("CDC", "NSN", "CDC")
                .setIngredient('C', Material.CHEST)
                .setIngredient('D', Material.DIAMOND_BLOCK)
                .setIngredient('N', Material.NETHERITE_INGOT)
                .setIngredient('S', new RecipeChoice.MaterialChoice(Tag.ITEMS_SIGNS))
        );
    }
}
