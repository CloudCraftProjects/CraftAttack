package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (20:05 27.10.2025)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.menu.AbstractMenu;
import dev.booky.craftattack.menu.ConfirmationMenu;
import dev.booky.craftattack.menu.Menu;
import dev.booky.craftattack.menu.MenuSlot;
import dev.booky.craftattack.menu.PagedMenu;
import dev.booky.craftattack.menu.context.MenuClickContext;
import dev.booky.craftattack.menu.result.MenuClickResult;
import dev.booky.craftattack.menu.result.MenuResult;
import dev.booky.craftattack.utils.PlayerHeadUtil;
import dev.booky.craftattack.utils.UniqueIdDataType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    private static final Map<Villager.Type, Material> VILL_TYPE_ITEMS = Map.ofEntries(
            Map.entry(Villager.Type.DESERT, Material.SAND),
            Map.entry(Villager.Type.JUNGLE, Material.JUNGLE_WOOD),
            Map.entry(Villager.Type.PLAINS, Material.SHORT_GRASS),
            Map.entry(Villager.Type.SAVANNA, Material.ACACIA_WOOD),
            Map.entry(Villager.Type.SNOW, Material.SNOW_BLOCK),
            Map.entry(Villager.Type.SWAMP, Material.MANGROVE_ROOTS),
            Map.entry(Villager.Type.TAIGA, Material.SPRUCE_WOOD)
    );
    private static final Map<Villager.Profession, Material> VILL_PROFESSION_ITEMS = Map.ofEntries(
            Map.entry(Villager.Profession.ARMORER, Material.BLAST_FURNACE),
            Map.entry(Villager.Profession.BUTCHER, Material.SMOKER),
            Map.entry(Villager.Profession.CARTOGRAPHER, Material.CARTOGRAPHY_TABLE),
            Map.entry(Villager.Profession.CLERIC, Material.BREWING_STAND),
            Map.entry(Villager.Profession.FARMER, Material.COMPOSTER),
            Map.entry(Villager.Profession.FISHERMAN, Material.BARREL),
            Map.entry(Villager.Profession.FLETCHER, Material.FLETCHING_TABLE),
            Map.entry(Villager.Profession.LEATHERWORKER, Material.CAULDRON),
            Map.entry(Villager.Profession.LIBRARIAN, Material.LECTERN),
            Map.entry(Villager.Profession.MASON, Material.STONECUTTER),
            Map.entry(Villager.Profession.NITWIT, Material.GREEN_BED),
            Map.entry(Villager.Profession.NONE, Material.STRUCTURE_VOID),
            Map.entry(Villager.Profession.SHEPHERD, Material.LOOM),
            Map.entry(Villager.Profession.TOOLSMITH, Material.SMITHING_TABLE),
            Map.entry(Villager.Profession.WEAPONSMITH, Material.GRINDSTONE)
    );

    private ShopMenu() {
    }

    public static boolean isValidIngredient(@Nullable ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getType() != EMPTY_INGREDIENT;
    }

    public static List<MerchantRecipe> buildRecipes(ShopVillager shop) {
        shop.updateStock();
        List<MerchantRecipe> recipes = shop.getMerchant().getRecipes();
        List<MerchantRecipe> filtered = new ArrayList<>(recipes.size());
        for (MerchantRecipe recipe : recipes) {
            ItemStack result = recipe.getResult();
            if (!isValidIngredient(result)) {
                continue; // no result
            }
            // filter valid ingredients
            List<ItemStack> validIngredients = new ArrayList<>();
            for (ItemStack ingredient : recipe.getIngredients()) {
                if (isValidIngredient(ingredient)) {
                    validIngredients.add(ingredient);
                }
            }
            if (validIngredients.isEmpty()) {
                continue; // no ingredients
            }
            // re-build recipe with unlimited uses (if available)
            int uses = shop.isTradeStocked(recipe) ? Integer.MAX_VALUE : 0;
            MerchantRecipe filteredRecipe = new MerchantRecipe(result, uses);
            filteredRecipe.setIngredients(validIngredients);
            filtered.add(filteredRecipe);
        }
        return filtered;
    }

    public static @Nullable AbstractVillager openMerchantMenu(Plugin plugin, ShopVillager shop, Player player) {
        List<MerchantRecipe> recipes = buildRecipes(shop);
        if (recipes.isEmpty()) {
            return null; // no recipes available
        }
        // spawn separate merchant per player to allow multiple players to access one shop at the same time
        AbstractVillager merchant = shop.getMerchant();
        WanderingTrader spawnedMerchant = merchant.getWorld().spawn(merchant.getLocation(), WanderingTrader.class, false, trader -> {
            trader.setPersistent(false);
            trader.setAI(false);
            trader.setSilent(true);
            trader.setVisibleByDefault(false);
            trader.setCollidable(false);
            // instantly despawn after player has closed the inventory
            trader.setDespawnDelay(1);
            trader.setRecipes(recipes);

            NamespacedKey refKey = new NamespacedKey(plugin, "shop/reference");
            trader.getPersistentDataContainer().set(refKey, UniqueIdDataType.INSTANCE, merchant.getUniqueId());
        });
        // build merchant inventory menu
        MenuType.MERCHANT.builder()
                .merchant(spawnedMerchant)
                .checkReachable(true)
                .title(translatable("ca.menu.shop.merchant"))
                .build(player).open();
        return spawnedMerchant; // report success!
    }

    public static void openMenu(ShopVillager shop, Player player) {
        shop.ensureLoaded();
        Menu.builder()
                .withTitle(translatable("ca.menu.shop.manage"))
                .withRows(3)
                // 00#000#00
                .withSlots(ctx -> ctx
                        .set(SLOTS_PER_ROW + 1,
                                itemStack(Material.LIME_HARNESS, translatable("ca.menu.shop.style")),
                                clickCtx -> {
                                    openStyleMenu(shop, clickCtx.getPlayer(), clickCtx.getMenu());
                                    return MenuClickResult.SOUND;
                                })
                        .set(SLOTS_PER_ROW + 3,
                                itemStack(Material.REDSTONE_TORCH, translatable("ca.menu.shop.manage-trades")),
                                clickCtx -> {
                                    openTradeManageMenu(shop, clickCtx.getPlayer(), clickCtx.getMenu());
                                    return MenuClickResult.SOUND;
                                })
                        .set(SLOTS_PER_ROW + SLOTS_PER_ROW - 4,
                                itemStack(Material.CHEST, translatable("ca.menu.shop.manage-stock")),
                                clickCtx -> {
                                    openStockMenu(shop, clickCtx.getPlayer(), clickCtx.getMenu());
                                    return new MenuClickResult(Sound.sound()
                                            .type(Key.key("block.chest.open"))
                                            .build());
                                })
                        .set(SLOTS_PER_ROW + SLOTS_PER_ROW - 2, buildDumpSlot(shop))
                        .set(SLOTS_PER_ROW * 3 - 1, buildDeleteSlot(shop))
                )
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
            clickCtx.update();
            return new MenuClickResult(Sound.sound()
                    .type(Key.key("entity.player.levelup"))
                    .build());
        });
    }

    public static MenuSlot buildDeleteSlot(ShopVillager shop) {
        return new MenuSlot(
                itemStack(Material.BARRIER, translatable("ca.menu.shop.delete")),
                clickCtx -> triggerDelete(clickCtx, shop, false)
        );
    }

    private static MenuClickResult triggerDelete(MenuClickContext ctx, ShopVillager shop, boolean confirmed) {
        AbstractVillager merchant = shop.getMerchant();
        if (shop.hasProfit()) {
            ctx.getPlayer().sendMessage(CaManager.getPrefix().append(translatable("ca.menu.shop.delete.profit-present")));
            return MenuClickResult.SOUND_FAIL.plus(MenuClickResult.CLOSE);
        } else if (shop.hasStock()) {
            ctx.getPlayer().sendMessage(CaManager.getPrefix().append(translatable("ca.menu.shop.delete.stock-present")));
            return MenuClickResult.SOUND_FAIL.plus(MenuClickResult.CLOSE);
        }
        if (confirmed) {
            merchant.setHealth(0d);
            for (Entity passenger : merchant.getPassengers()) {
                passenger.remove();
            }
            ctx.getPlayer().sendMessage(CaManager.getPrefix().append(translatable("ca.menu.shop.delete.success")));
        } else {
            ConfirmationMenu.builder()
                    .withRows(3)
                    .withConfirmHandler(confirmCtx -> triggerDelete(confirmCtx, shop, true))
                    .withTitle(translatable("ca.menu.shop.delete.confirm.title"))
                    .withQuestion(translatable("ca.menu.shop.delete.confirm.question"))
                    .withParent(ctx.getMenu())
                    .open(ctx.getPlayer());
        }
        return MenuClickResult.CLOSE_SOUND;
    }

    public static void openStyleMenu(ShopVillager shop, Player player, @Nullable AbstractMenu parent) {
        shop.ensureLoaded();
        Menu.builder()
                .withTitle(translatable("ca.menu.shop.style"))
                .withRows(3)
                .withSlots(ctx -> {
                    AbstractVillager merchant = shop.getMerchant();
                    if (merchant instanceof Villager villager) {
                        // biome changer
                        ctx.set(SLOTS_PER_ROW + 3,
                                itemStack(
                                        VILL_TYPE_ITEMS.get(villager.getVillagerType()),
                                        translatable("ca.menu.shop.style.biome",
                                                translatable("ca.menu.shop.style.biome." + villager.getVillagerType().key().value()))),
                                buildStyleHandler(
                                        Registry.VILLAGER_TYPE,
                                        () -> ((Villager) shop.getMerchant()).getVillagerType(),
                                        type -> ((Villager) shop.getMerchant()).setVillagerType(type)));
                        // profession changer
                        ctx.set(SLOTS_PER_ROW + 3 + 2,
                                itemStack(
                                        VILL_PROFESSION_ITEMS.get(villager.getProfession()),
                                        translatable("ca.menu.shop.style.profession",
                                                translatable(villager.getProfession()))),
                                buildStyleHandler(
                                        Registry.VILLAGER_PROFESSION,
                                        () -> ((Villager) shop.getMerchant()).getProfession(),
                                        prof -> {
                                            // trades reset when changing profession, so temporarily save recipes
                                            Villager entity = (Villager) shop.getMerchant();
                                            List<MerchantRecipe> recipes = entity.getRecipes();
                                            entity.setProfession(prof);
                                            entity.setRecipes(recipes);
                                        }));
                    }
                })
                .withParent(parent)
                .open(player);
    }

    public static <T extends Keyed> Function<MenuClickContext, MenuClickResult> buildStyleHandler(
            Registry<T> registry, Supplier<T> getter, Consumer<T> setter
    ) {
        List<T> values = registry.stream().toList();
        return ctx -> {
            int offset = !ctx.getClickType().isRightClick() ? 1 : values.size() - 1;
            int newOrdinal = (values.indexOf(getter.get()) + offset) % values.size();
            setter.accept(values.get(newOrdinal));
            ctx.update();
            return MenuClickResult.SOUND;
        };
    }

    public static void openStockMenu(ShopVillager shop, Player player, @Nullable AbstractMenu parent) {
        shop.ensureLoaded();
        InventoryView view = Menu.builder()
                .withTitle(translatable("ca.menu.shop.manage-stock"))
                .withRows(6)
                .withSlots(ctx -> {
                    shop.ensureLoaded();
                    List<ItemStack> stock = shop.getStock();
                    @Nullable MenuSlot[] slots = ctx.getSlots();
                    for (int i = 0, j = 0; j < slots.length; i++) {
                        ItemStack stack = i < stock.size() ? stock.get(i) : ItemStack.empty();
                        if (j == slots.length - SLOTS_PER_ROW) {
                            j++; // don't override back button
                        }
                        ctx.set(j++, new MenuSlot(stack, clickCtx -> {
                            // prevent exploits by ensuring the shop is loaded
                            // at all times while this inventory is being interacted with
                            shop.ensureLoaded();
                            return MenuClickResult.ALLOW;
                        }));
                    }
                })
                .onClose(ctx -> {
                    shop.ensureLoaded();
                    List<ItemStack> stock = new ArrayList<>();
                    @Nullable ItemStack[] contents = ctx.getInventory().getContents();
                    for (int i = 0; i < contents.length; i++) {
                        if (i == contents.length - SLOTS_PER_ROW) {
                            continue; // don't override back button
                        }
                        ItemStack stack = contents[i];
                        if (stack != null && !stack.isEmpty()) {
                            stock.add(stack.clone());
                        }
                    }
                    shop.setStock(stock);
                    shop.setStock((InventoryView) null);
                    return MenuResult.NONE;
                })
                .withParent(parent)
                .withStorage(true)
                .open(player);
        shop.setStock(view);
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

    private static ItemStack getCursorStack(MenuClickContext ctx) {
        ItemStack cursor = ctx.getCursor();
        if (!isValidIngredient(cursor)) {
            return ItemStack.of(EMPTY_INGREDIENT);
        }
        if (ctx.getClickType().isRightClick()) {
            return cursor.asOne();
        }
        return cursor;
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
            if (!clickCtx.getClickType().isMouseClick()) {
                return MenuClickResult.NONE; // ignore non-mouse clicks
            }
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
            ingredients.set(index, getCursorStack(clickCtx));
            trade.setIngredients(ingredients);
            merchant.setRecipe(tradeIndex, trade);
            // update inventory content
            clickCtx.update();
            return MenuClickResult.SOUND;
        });
    }

    private static MenuSlot buildResultSlot(int tradeIndex, MerchantRecipe recipe, ShopVillager shop) {
        return new MenuSlot(ctx -> {
            shop.ensureLoaded();
            return modifyIngredient(recipe.getResult());
        }, clickCtx -> {
            if (!clickCtx.getClickType().isMouseClick()) {
                return MenuClickResult.NONE; // ignore non-mouse clicks
            }
            // we have to re-create the recipe
            AbstractVillager merchant = shop.getMerchant();
            MerchantRecipe trade = merchant.getRecipe(tradeIndex);
            if (clickCtx.getCursor().equals(trade.getResult())) {
                return MenuClickResult.NONE;
            }
            ItemStack newResult = getCursorStack(clickCtx);
            MerchantRecipe newTrade = new MerchantRecipe(newResult, 0);
            newTrade.setIngredients(trade.getIngredients());
            merchant.setRecipe(tradeIndex, newTrade);
            // update inventory content
            clickCtx.update();
            return MenuClickResult.SOUND;
        });
    }

    private static MenuSlot buildNewTradeSlot(ShopVillager shop) {
        return new MenuSlot(ItemStack.of(Material.STONE), clickCtx -> {
            AbstractVillager merchant = shop.getMerchant();
            int recipeCount = merchant.getRecipeCount();
            if (recipeCount >= MAX_TRADES) {
                clickCtx.update();
                return MenuClickResult.NONE;
            }
            // create dummy recipe
            MerchantRecipe dummyRecipe = new MerchantRecipe(ItemStack.of(EMPTY_INGREDIENT), 0);
            dummyRecipe.addIngredient(ItemStack.of(EMPTY_INGREDIENT));
            // create new list for all recipes... there is no addRecipe method
            List<MerchantRecipe> allRecipes = new ArrayList<>(merchant.getRecipes());
            allRecipes.add(dummyRecipe);
            merchant.setRecipes(allRecipes);

            clickCtx.update();
            return MenuClickResult.SOUND;
        });
    }
}
