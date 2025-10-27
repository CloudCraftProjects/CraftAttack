package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (20:05 27.10.2025)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.menu.Menu;
import dev.booky.craftattack.menu.result.MenuClickResult;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static dev.booky.craftattack.menu.AbstractMenu.SLOTS_PER_ROW;
import static dev.booky.craftattack.utils.ItemStackUtil.itemStack;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.TranslationArgument.numeric;

@NullMarked
public final class ShopMenu {

    private static final int MAX_PROFIT_ENTRIES = 10;

    private ShopMenu() {
    }

    public static void openMenu(ShopVillager shop, Player player) {
        Menu.builder()
                .withTitle(translatable("ca.menu.shop.manage"))
                .withRows(3)
                // 00#000#00
                .withSlots(ctx -> ctx
                        .set(SLOTS_PER_ROW + 2,
                                itemStack(Material.REDSTONE_TORCH, translatable("ca.menu.shop.manage-trades")),
                                clickCtx -> {
                                    openTradeManageMenu(shop, clickCtx.getPlayer());
                                    return MenuClickResult.SOUND;
                                })
                        .set(SLOTS_PER_ROW * 2 - 3, supplyCtx -> {
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
                            if (!shop.hasProfit()) {
                                return new MenuClickResult(Sound.sound()
                                        .type(Key.key("entity.villager.no"))
                                        .build());
                            }
                            boolean dumpedEverything = shop.dumpProfit(player.getInventory());
                            if (!dumpedEverything) {
                                player.sendMessage(CaManager.getPrefix().append(
                                        translatable("ca.shop.manage.dump.inventory-full")));
                                return new MenuClickResult(Sound.sound()
                                        .type(Key.key("entity.villager.no"))
                                        .build(), true, false);
                            }
                            return new MenuClickResult(Sound.sound()
                                    .type(Key.key("entity.player.levelup"))
                                    .build(), true, false);
                        }))
                .open(player);
    }

    public static void openTradeManageMenu(ShopVillager villager, Player player) {

    }
}
