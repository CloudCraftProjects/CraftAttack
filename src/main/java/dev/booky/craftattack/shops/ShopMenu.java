package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (20:05 27.10.2025)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.menu.AbstractMenu;
import dev.booky.craftattack.menu.Menu;
import dev.booky.craftattack.menu.MenuSlot;
import dev.booky.craftattack.menu.PagedMenu;
import dev.booky.craftattack.menu.result.MenuClickResult;
import dev.booky.craftattack.menu.result.MenuResult;
import dev.booky.craftattack.utils.PlayerHeadUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.MerchantRecipe;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

import static dev.booky.craftattack.menu.AbstractMenu.SLOTS_PER_ROW;
import static dev.booky.craftattack.utils.ItemStackUtil.itemStack;
import static dev.booky.craftattack.utils.PlayerHeadUtil.WHITE_ARROW_RIGHT_TEXTURE;
import static dev.booky.craftattack.utils.PlayerHeadUtil.WHITE_PLUS_TEXTURE;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.TranslationArgument.numeric;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@NullMarked
public final class ShopMenu {

    private static final int MAX_PROFIT_ENTRIES = 10;
    private static final int TRADES_PER_PAGE = 4;
    private static final int MAX_TRADES = 20;
    public static final Material EMPTY_INGREDIENT = Material.STRUCTURE_VOID;

    private ShopMenu() {
    }

    public static boolean isValidIngredient(@Nullable ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getType() != EMPTY_INGREDIENT;
    }

    public static void openMerchantMenu(ShopVillager shop, Player player) {
        // TODO create per-player merchant
        MenuType.MERCHANT.builder()
                .merchant(shop.getMerchant())
                .checkReachable(true)
                .title(translatable("ca.menu.shop.merchant"))
                .build(player)
                .open();
    }

    public static void openMenu(ShopVillager shop, Player player) {
        shop.ensureLoaded();
        Menu.builder()
                .withTitle(translatable("ca.menu.shop.manage"))
                .withRows(3)
                // 00#000#00
                .withSlots(ctx -> ctx
                        .set(SLOTS_PER_ROW + 2,
                                itemStack(Material.REDSTONE_TORCH, translatable("ca.menu.shop.manage-trades")),
                                clickCtx -> {
                                    openTradeManageMenu(shop, clickCtx.getPlayer(), clickCtx.getMenu());
                                    return MenuClickResult.SOUND;
                                })
                        .set(SLOTS_PER_ROW + SLOTS_PER_ROW / 2,
                                itemStack(Material.CHEST, translatable("ca.menu.shop.manage-stock")),
                                clickCtx -> {
                                    openStockMenu(shop, clickCtx.getPlayer(), clickCtx.getMenu());
                                    return new MenuClickResult(Sound.sound()
                                            .type(Key.key("block.chest.open"))
                                            .build());
                                })
                        .set(SLOTS_PER_ROW * 2 - 3, buildDumpSlot(shop)))
                .open(player);
    }

    private static MenuSlot buildDumpSlot(ShopVillager shop) {
        return new MenuSlot(supplyCtx -> {
            shop.ensureLoaded();
            List<ItemStack> profit = shop.getProfit();
            if (profit.isEmpty()) {
                return itemStack(Material.STRUCTURE_VOID,
                        translatable("ca.menu.shop.dump-profit"),
                        translatable("ca.menu.shop.dump-profit.no-profit"));
            }
            List<Component> lore = new ArrayList<>(profit.size());
            List<Material> stackTypes = new ArrayList<>(profit.size());
            for (ItemStack stack : profit) {
                if (lore.size() == MAX_PROFIT_ENTRIES) {
                    lore.add(translatable("ca.menu.shop.dump-profit.more",
                            numeric(profit.size() - lore.size())));
                } else if (lore.size() < MAX_PROFIT_ENTRIES) {
                    lore.add(translatable("ca.menu.shop.dump-profit.profit-entry",
                            numeric(stack.getAmount()), stack.effectiveName()));
                }
                stackTypes.add(stack.getType());
            }
            Material material = stackTypes.get(ThreadLocalRandom.current().nextInt(stackTypes.size()));
            ItemStack stack = itemStack(material, translatable("ca.menu.shop.dump-profit"), lore);
            stack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            return stack;
        }, clickCtx -> {
            shop.ensureLoaded();
            if (!shop.hasProfit()) {
                return new MenuClickResult(Sound.sound()
                        .type(Key.key("entity.villager.no"))
                        .build());
            }
            boolean dumpedEverything = shop.dumpProfit(clickCtx.getPlayer().getInventory());
            if (!dumpedEverything) {
                clickCtx.getPlayer().sendMessage(CaManager.getPrefix().append(
                        translatable("ca.shop.manage.dump.inventory-full")));
            }
            AbstractMenu.updateContent(clickCtx.getPlayer());
            return new MenuClickResult(Sound.sound()
                    .type(Key.key("entity.player.levelup"))
                    .build());
        });
    }

    public static void openStockMenu(ShopVillager shop, Player player, @Nullable AbstractMenu parent) {
        shop.ensureLoaded();
        Menu.builder()
                .withTitle(translatable("ca.menu.shop.manage-stock"))
                .withRows(6)
                .withSlots(ctx -> {
                    shop.ensureLoaded();
                    List<ItemStack> stock = shop.getStock();
                    for (int i = 0; i < stock.size(); i++) {
                        if (i == ctx.getSlots().length - SLOTS_PER_ROW) {
                            i++; // don't override back button
                            continue;
                        }
                        ctx.set(i, new MenuSlot(stock.get(i),
                                clickCtx -> MenuClickResult.ALLOW));
                    }
                })
                .onClose(ctx -> {
                    shop.ensureLoaded();
                    List<ItemStack> stock = new ArrayList<>();
                    @Nullable ItemStack[] contents = ctx.getInventory().getContents();
                    for (int i = 0; i < contents.length; i++) {
                        if (i == contents.length - SLOTS_PER_ROW) {
                            i++; // don't override back button
                            continue;
                        }
                        ItemStack stack = contents[i];
                        if (stack != null && !stack.isEmpty()) {
                            stock.add(stack.clone());
                        }
                    }
                    shop.setStock(stock);
                    return MenuResult.NONE;
                })
                .withParent(parent)
                .withStorage(true)
                .open(player);
    }

    public static void openTradeManageMenu(ShopVillager shop, Player player, @Nullable AbstractMenu parent) {
        shop.ensureLoaded();
        PagedMenu.builder()
                .withRows(TRADES_PER_PAGE + 2)
                .withTitle(translatable("ca.menu.shop.manage-trades"))
                .withSlots(ctx -> {
                    int tradeIndex = 0;
                    Queue<@Nullable MerchantRecipe> trades = new LinkedList<>(shop.getMerchant().getRecipes());
                    if (trades.size() < MAX_TRADES) {
                        trades.add(null); // add null trade last to signal that this is the last trade (marker for new-trade-slot)
                    }

                    MenuSlot emptySlot = MenuSlot.createEmptySlot();
                    MenuSlot plusSlot = new MenuSlot(PlayerHeadUtil.createHeadStack(WHITE_PLUS_TEXTURE, empty()));
                    MenuSlot arrowSlot = new MenuSlot(PlayerHeadUtil.createHeadStack(WHITE_ARROW_RIGHT_TEXTURE, empty()));
                    while (!trades.isEmpty()) {
                        for (int i = 0; i < SLOTS_PER_ROW; i++) {
                            ctx.add(emptySlot);
                        }
                        int i;
                        for (i = 0; i < TRADES_PER_PAGE && !trades.isEmpty(); i++) {
                            MerchantRecipe recipe = trades.remove();
                            if (recipe == null) {
                                // last trade, add "plus" bottom to add more trades
                                ctx.add(emptySlot, emptySlot, emptySlot, emptySlot);
                                ctx.add(buildNewTradeSlot(shop));
                                ctx.add(emptySlot, emptySlot, emptySlot, emptySlot);
                                continue;
                            }
                            // 0IOIOROFO
                            ctx.add(emptySlot, emptySlot);
                            ctx.add(buildIngredientSlot(tradeIndex, 0, recipe, shop));
                            ctx.add(plusSlot, buildIngredientSlot(tradeIndex, 1, recipe, shop));
                            ctx.add(arrowSlot, buildResultSlot(tradeIndex, recipe, shop));
                            ctx.add(emptySlot, emptySlot);
                            tradeIndex++;
                        }
                        if (i == TRADES_PER_PAGE) {
                            for (int j = 2; j < SLOTS_PER_ROW; j++) {
                                ctx.add(emptySlot);
                            }
                        }
                    }
                })
                .withParent(parent)
                .open(player);
    }

    private static ItemStack wrapIngredient(@Nullable ItemStack stack) {
        if (!isValidIngredient(stack)) {
            return ItemStack.of(EMPTY_INGREDIENT);
        }
        return stack;
    }

    private static ItemStack modifyIngredient(@Nullable ItemStack stack) {
        if (!isValidIngredient(stack)) {
            stack = itemStack(EMPTY_INGREDIENT, translatable("ca.menu.shop.manage.ingredient.none"));
        }
        // modify lore to include info
        List<Component> lines = new ArrayList<>();
        ItemLore lore = stack.getData(DataComponentTypes.LORE);
        if (lore != null) {
            lines.addAll(lore.lines());
            lines.add(empty()); // empty line separator
        }
        lines.add(translatable("ca.menu.shop.manage.ingredient.desc")
                .decoration(ITALIC, false));
        stack.setData(DataComponentTypes.LORE, ItemLore.lore(lines));
        return stack;
    }

    private static MenuSlot buildIngredientSlot(int tradeIndex, int index, MerchantRecipe recipe, ShopVillager shop) {
        return new MenuSlot(ctx -> {
            shop.ensureLoaded();
            List<ItemStack> ingredients = recipe.getIngredients();
            return modifyIngredient(index >= ingredients.size() ? null : ingredients.get(index));
        }, clickCtx -> {
            // extract up-to-date ingredients
            AbstractVillager merchant = shop.getMerchant();
            MerchantRecipe trade = merchant.getRecipe(tradeIndex);
            List<ItemStack> ingredients = new ArrayList<>(trade.getIngredients());
            while (ingredients.size() <= index) {
                ingredients.add(ItemStack.of(EMPTY_INGREDIENT));
            }
            // verify something actually changed
            if (clickCtx.getCursor().equals(ingredients.get(index))) {
                return MenuClickResult.NONE;
            }
            // update ingredients
            ingredients.set(index, wrapIngredient(clickCtx.getCursor()));
            trade.setIngredients(ingredients);
            merchant.setRecipe(tradeIndex, trade);
            // update inventory content
            AbstractMenu.updateContent(clickCtx.getPlayer());
            return MenuClickResult.SOUND;
        });
    }

    private static MenuSlot buildResultSlot(int tradeIndex, MerchantRecipe recipe, ShopVillager shop) {
        return new MenuSlot(ctx -> {
            shop.ensureLoaded();
            return modifyIngredient(recipe.getResult());
        }, clickCtx -> {
            // we have to re-create the recipe
            AbstractVillager merchant = shop.getMerchant();
            MerchantRecipe trade = merchant.getRecipe(tradeIndex);
            if (clickCtx.getCursor().equals(trade.getResult())) {
                return MenuClickResult.NONE;
            }
            ItemStack newResult = wrapIngredient(clickCtx.getCursor());
            MerchantRecipe newTrade = new MerchantRecipe(newResult, 0);
            newTrade.setIngredients(trade.getIngredients());
            merchant.setRecipe(tradeIndex, newTrade);
            // update inventory content
            AbstractMenu.updateContent(clickCtx.getPlayer());
            return MenuClickResult.SOUND;
        });
    }

    private static MenuSlot buildNewTradeSlot(ShopVillager shop) {
        return new MenuSlot(ItemStack.of(Material.STONE), clickCtx -> {
            AbstractVillager merchant = shop.getMerchant();
            int recipeCount = merchant.getRecipeCount();
            if (recipeCount >= MAX_TRADES) {
                AbstractMenu.updateContent(clickCtx.getPlayer());
                return MenuClickResult.NONE;
            }
            // create dummy recipe
            MerchantRecipe dummyRecipe = new MerchantRecipe(ItemStack.of(EMPTY_INGREDIENT), 0);
            dummyRecipe.addIngredient(ItemStack.of(EMPTY_INGREDIENT));
            // create new list for all recipes... there is no addRecipe method
            List<MerchantRecipe> allRecipes = new ArrayList<>(merchant.getRecipes());
            allRecipes.add(dummyRecipe);
            merchant.setRecipes(allRecipes);

            AbstractMenu.updateContent(clickCtx.getPlayer());
            return MenuClickResult.SOUND;
        });
    }
}
