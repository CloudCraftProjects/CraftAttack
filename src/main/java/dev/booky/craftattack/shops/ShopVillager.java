package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (23:07 26.10.2025)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.ItemStackListDataType;
import dev.booky.craftattack.utils.UniqueIdDataType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.joml.Matrix4f;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.translatable;

@NullMarked
public final class ShopVillager {

    private final WeakReference<AbstractVillager> merchant;
    private final UUID merchantId;
    private final Consumer<ShopVillager> onDirty;

    private final NamespacedKey profitKey;
    private final List<ItemStack> profit = new ArrayList<>();
    private final NamespacedKey stockKey;
    private final List<ItemStack> stock = new ArrayList<>();

    private final NamespacedKey ownerKey;
    private @Nullable UUID ownerId;

    public ShopVillager(CaManager manager, AbstractVillager merchant, Consumer<ShopVillager> onDirty) {
        this.merchant = new WeakReference<>(merchant);
        this.merchantId = merchant.getUniqueId();
        this.onDirty = onDirty;

        this.profitKey = new NamespacedKey(manager.getPlugin(), "shop/profit");
        this.stockKey = new NamespacedKey(manager.getPlugin(), "shop/stock");
        this.ownerKey = new NamespacedKey(manager.getPlugin(), "shop/owner");

        this.loadData();
    }

    public static Villager spawnShop(Location location, LivingEntity owner, Plugin plugin) {
        NamespacedKey shopKey = new NamespacedKey(plugin, "shop");
        NamespacedKey ownerKey = new NamespacedKey(plugin, "shop/owner");
        Villager ret = location.getWorld().spawn(location, Villager.class, false, villager -> {
            PersistentDataContainer pdc = villager.getPersistentDataContainer();
            pdc.set(shopKey, PersistentDataType.BOOLEAN, true);
            pdc.set(ownerKey, UniqueIdDataType.INSTANCE, owner.getUniqueId());

            villager.setAI(false);
            villager.setRemoveWhenFarAway(false);
            villager.setInvulnerable(true);

            villager.addPotionEffect(new PotionEffect(
                    PotionEffectType.FIRE_RESISTANCE,
                    PotionEffect.INFINITE_DURATION,
                    0, false, false, false
            ));

            ShopListener.lookAt(villager, owner);
        });
        location.getWorld().spawn(location, TextDisplay.class, false, display -> {
            display.setShadowed(true);
            display.setBackgroundColor(Color.fromARGB(0x00000000));
            display.setBillboard(Display.Billboard.CENTER);
            display.text(translatable("ca.shop.display", owner.teamDisplayName()));
            display.setTransformationMatrix(new Matrix4f()
                    .translateLocal(0f, 0.2f, 0f));
            ret.addPassenger(display);
        });
        return ret;
    }

    public boolean tryTrade(MerchantRecipe recipe) {
        if (this.isTradeStocked(recipe)) {
            this.consumeStock(recipe.getResult());
            this.addProfit(recipe);
            return true;
        }
        return false;
    }

    public boolean isTradeStocked(MerchantRecipe recipe) {
        ItemStack result = recipe.getResult();
        if (!ShopMenu.isValidIngredient(result)) {
            return false; // no result set
        }
        for (ItemStack stack : this.stock) {
            if (!result.isSimilar(stack)) {
                continue; // doesn't match
            }
            result.subtract(stack.getAmount()); // consume
            if (result.isEmpty()) {
                break; // finished consuming
            }
        }
        return result.isEmpty(); // check whether result got consumed
    }

    public void consumeStock(ItemStack stack) {
        stack = stack.clone();
        boolean mut = false;
        for (ItemStack stock : this.stock) {
            if (!stack.isSimilar(stock)) {
                continue; // doesn't match
            }
            int newAmount = Math.max(0, stack.getAmount() - stock.getAmount());
            stock.subtract(stack.getAmount());
            stack.setAmount(newAmount);
            mut = true;
            if (stack.isEmpty()) {
                break;
            }
        }
        if (mut) {
            this.markDirty();
        }
    }

    public void addProfit(MerchantRecipe recipe) {
        for (ItemStack ingredient : recipe.getIngredients()) {
            if (ShopMenu.isValidIngredient(ingredient)) {
                this.addProfit(ingredient);
            }
        }
    }

    public void addProfit(ItemStack profit) {
        this.ensureLoaded();
        if (profit.isEmpty()) {
            return; // useless
        }
        profit = profit.clone(); // don't mutate parameter
        for (ItemStack stack : this.profit) {
            int maxStack = stack.getMaxStackSize();
            if (stack.getAmount() >= maxStack) {
                continue; // full slot
            } else if (!stack.isSimilar(profit)) {
                continue; // stacks don't match
            }
            // check how much fits into this slot
            int count = Math.min(maxStack - stack.getAmount(), profit.getAmount());
            profit.subtract(count);
            stack.add(count);
            if (profit.isEmpty()) {
                break;
            }
        }
        // check if there is still some part of it remaining
        if (!profit.isEmpty()) {
            this.profit.add(profit);
        }
        // mark as dirty
        this.markDirty();
    }

    public boolean hasStock() {
        return !this.stock.isEmpty();
    }

    public List<ItemStack> getStock() {
        return this.stock;
    }

    public void setStock(List<ItemStack> stock) {
        this.stock.clear();
        this.stock.addAll(stock);
        this.markDirty();
    }

    public boolean hasProfit() {
        return !this.profit.isEmpty();
    }

    public List<ItemStack> getProfit() {
        return this.profit;
    }

    public boolean dumpProfit(Inventory inventory) {
        this.ensureLoaded();
        boolean mut = false;
        boolean ret = true;
        Iterator<ItemStack> it = this.profit.iterator();
        while (it.hasNext()) {
            ItemStack stack = it.next();
            // try adding to inventory
            Map<Integer, ItemStack> result = inventory.addItem(stack.clone());
            if (result.isEmpty()) {
                mut = true;
                it.remove(); // item was dumped into inventory
                continue; // continue dumping the remaining profit
            }
            // update stack amount
            int remainingAmount = result.get(0).getAmount();
            if (remainingAmount != stack.getAmount()) {
                mut = true;
                stack.setAmount(remainingAmount);
            }
            // inventory is full, we failed to dump everything
            ret = false;
        }
        if (mut) {
            this.markDirty();
        }
        return ret; // success if no errors occur
    }

    private void loadData() {
        PersistentDataContainer pdc = this.getMerchant().getPersistentDataContainer();
        // load profit stacks
        ItemStack[] profitStacks = pdc.get(this.profitKey, ItemStackListDataType.INSTANCE);
        this.profit.clear();
        if (profitStacks != null && profitStacks.length > 0) {
            this.profit.addAll(List.of(profitStacks));
        }
        // load stock stacks
        ItemStack[] stockStacks = pdc.get(this.stockKey, ItemStackListDataType.INSTANCE);
        this.stock.clear();
        if (stockStacks != null && stockStacks.length > 0) {
            this.stock.addAll(List.of(stockStacks));
        }
        // load id of owner
        this.ownerId = pdc.get(this.ownerKey, UniqueIdDataType.INSTANCE);
    }

    public void saveData() {
        PersistentDataContainer pdc = this.getMerchant().getPersistentDataContainer();
        // save profit stacks
        if (!this.profit.isEmpty()) {
            pdc.set(this.profitKey, ItemStackListDataType.INSTANCE,
                    this.profit.toArray(new ItemStack[0]));
        } else {
            pdc.remove(this.profitKey);
        }
        // save stock stacks
        if (!this.stock.isEmpty()) {
            pdc.set(this.stockKey, ItemStackListDataType.INSTANCE,
                    this.stock.toArray(new ItemStack[0]));
        } else {
            pdc.remove(this.stockKey);
        }
        // save id of owner
        if (this.ownerId != null) {
            pdc.set(this.ownerKey, UniqueIdDataType.INSTANCE, this.ownerId);
        } else {
            pdc.remove(this.ownerKey);
        }
    }

    public void markDirty() {
        this.onDirty.accept(this);
    }

    public void ensureLoaded() {
        this.getMerchant();
    }

    public AbstractVillager getMerchant() {
        AbstractVillager merchant = this.merchant.get();
        if (merchant == null || !merchant.isValid()) {
            throw new IllegalStateException("Shop villager " + merchant + " (" + this.merchantId + ") is no longer valid");
        }
        return merchant;
    }

    public boolean isOwner(Entity entity) {
        if (this.ownerId == null) {
            return true; // freely usable? Idk how this might happen
        }
        return this.ownerId.equals(entity.getUniqueId());
    }

    public @Nullable UUID getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(@Nullable UUID ownerId) {
        if (!Objects.equals(this.ownerId, ownerId)) {
            this.ownerId = ownerId;
            this.markDirty();
        }
    }
}
