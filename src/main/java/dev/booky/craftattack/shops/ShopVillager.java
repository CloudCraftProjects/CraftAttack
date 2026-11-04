package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (23:07 26.10.2025)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.ItemStackListDataType;
import dev.booky.craftattack.utils.UniqueIdDataType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.translatable;

@NullMarked
public final class ShopVillager {

    private final WeakReference<AbstractVillager> merchant;
    private final UUID merchantId;
    private final Consumer<ShopVillager> onDirty;

    private final NamespacedKey profitKey;
    private final NamespacedKey stockKey;
    private final NamespacedKey ownerKey;

    private final Lock stateLock = new ReentrantLock();
    private final NonNullList<ItemStack> profit = NonNullList.create();
    private final NonNullList<ItemStack> stock = NonNullList.create();
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

    public static org.bukkit.inventory.ItemStack createSpawnEgg(Plugin plugin) {
        NamespacedKey shopKey = new NamespacedKey(plugin, "shop");
        org.bukkit.inventory.ItemStack stack = org.bukkit.inventory.ItemStack.of(Material.VILLAGER_SPAWN_EGG);
        stack.editMeta(meta -> {
            meta.itemName(translatable("ca.shop.spawn-egg"));
            meta.setEnchantmentGlintOverride(true);
            // can't be set using data components
            meta.getPersistentDataContainer().set(shopKey, PersistentDataType.BOOLEAN, true);
        });
        return stack;
    }

    public static Villager spawnShop(Location location, LivingEntity owner, Plugin plugin) {
        if (!Bukkit.isOwnedByCurrentRegion(location)) {
            throw new IllegalStateException("Location " + location + " needs to be owned for shop to spawn");
        }
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

            // just ignore the look if the owner is too far away
            if (Bukkit.isOwnedByCurrentRegion(owner)) {
                ShopListener.lookAt(villager, owner);
            }
        });
        TextDisplay shopText = location.getWorld().spawn(location, TextDisplay.class, false, display -> {
            display.setShadowed(true);
            display.setBackgroundColor(Color.fromARGB(0x00000000));
            display.setBillboard(Display.Billboard.CENTER);
            display.text(translatable("ca.shop.display", owner.name()));
            display.setTransformationMatrix(new Matrix4f()
                    .translateLocal(0f, 0.2f, 0f));
            ret.addPassenger(display);
            // try to prevent flickering of text display by not showing it until it has been spawned
            display.setVisibleByDefault(false);
        });
        shopText.setVisibleByDefault(true);
        return ret;
    }

    public boolean tryTrade(MerchantRecipe recipe) {
        if (this.isTradeStocked(recipe)) {
            this.consumeStock(CraftItemStack.unwrap(recipe.getResult()));
            this.addProfit(recipe);
            return true;
        }
        return false;
    }

    public boolean isTradeStocked(MerchantRecipe recipe) {
        ItemStack result = CraftItemStack.unwrap(recipe.getResult()).copy();
        if (!ShopMenu.isValidIngredient(result)) {
            return false; // no result set
        }
        for (ItemStack stack : this.stock) {
            if (!ItemStack.isSameItemSameComponents(result, stack)) {
                continue; // doesn't match
            }
            result.shrink(stack.getCount()); // consume
            if (result.isEmpty()) {
                break; // finished consuming
            }
        }
        return result.isEmpty(); // check whether result got consumed
    }

    public void consumeStock(ItemStack stack) {
        int remaining = stack.getCount();
        boolean mut = false;
        for (ItemStack stock : this.stock) {
            if (remaining <= 0) {
                break; // finished consuming
            }
            if (!ItemStack.isSameItemSameComponents(stock, stack)) {
                continue; // doesn't match
            }
            int amount = Math.min(remaining, stock.getCount());
            if (amount <= 0) {
                continue;
            }
            stock.shrink(amount);
            remaining -= amount;
            mut = true;
        }
        if (mut) {
            this.markDirty();
        }
    }

    public void addProfit(MerchantRecipe recipe) {
        for (org.bukkit.inventory.ItemStack ingredient : recipe.getIngredients()) {
            if (ShopMenu.isValidIngredient(ingredient)) {
                this.addProfit(CraftItemStack.unwrap(ingredient));
            }
        }
    }

    public void addProfit(ItemStack profit) {
        this.ensureLoaded();
        if (profit.isEmpty()) {
            return; // useless
        }
        profit = profit.copy(); // don't mutate parameter
        for (ItemStack stack : this.profit) {
            int maxStack = stack.getMaxStackSize();
            if (stack.getCount() >= maxStack) {
                continue; // full slot
            } else if (!ItemStack.isSameItemSameComponents(stack, profit)) {
                continue; // stacks don't match
            }
            // check how much fits into this slot
            int count = Math.min(maxStack - stack.getCount(), profit.getCount());
            profit.shrink(count);
            stack.grow(count);
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

    @ApiStatus.Internal
    public Lock getStateLock() {
        return this.stateLock;
    }

    public boolean hasStock() {
        for (ItemStack stack : this.stock) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void padStock(int slot) {
        if (slot < 0 || slot >= ShopMenu.MAX_STOCK_SLOTS) {
            throw new IndexOutOfBoundsException("Stock slot " + slot
                    + " is out of bounds (max " + (ShopMenu.MAX_STOCK_SLOTS - 1) + ")");
        }
        int n = slot - this.stock.size() + 1;
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                this.stock.add(new ItemStack(Items.AIR));
            }
            this.markDirty();
        }
    }

    public int getStockCount() {
        return this.stock.size();
    }

    public ItemStack getStockStack(int slot) {
        this.padStock(slot);
        return this.stock.get(slot);
    }

    public void setStockStack(int slot, ItemStack stack) {
        this.padStock(slot);
        this.stock.set(slot, stack);
        this.markDirty();
    }

    public boolean hasProfit() {
        for (ItemStack stack : this.profit) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
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
            if (stack.isEmpty()) {
                mut = true;
                it.remove();
                continue;
            }
            org.bukkit.inventory.ItemStack bukkitStack = CraftItemStack.asBukkitCopy(stack);
            // try adding to inventory
            Map<Integer, org.bukkit.inventory.ItemStack> result = inventory.addItem(bukkitStack);
            if (result.isEmpty()) {
                mut = true;
                it.remove(); // item was dumped into inventory
                continue; // continue dumping the remaining profit
            }
            // update stack amount
            int remainingAmount = result.get(0).getAmount();
            if (remainingAmount != stack.getCount()) {
                mut = true;
                stack.setCount(remainingAmount);
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
        org.bukkit.inventory.ItemStack[] profitStacks = pdc.get(this.profitKey, ItemStackListDataType.INSTANCE);
        this.profit.clear();
        if (profitStacks != null) {
            for (org.bukkit.inventory.ItemStack stack : profitStacks) {
                ItemStack item = CraftItemStack.unwrap(stack);
                // legacy data may contain empty stacks, they don't contain any profit
                if (!item.isEmpty()) {
                    this.profit.add(item);
                }
            }
        }
        // load stock stacks
        org.bukkit.inventory.ItemStack[] stockStacks = pdc.get(this.stockKey, ItemStackListDataType.INSTANCE);
        this.stock.clear();
        if (stockStacks != null) {
            for (org.bukkit.inventory.ItemStack stack : stockStacks) {
                this.stock.add(CraftItemStack.unwrap(stack));
            }
        }
        // trailing empty stacks are only padding added by the stock menu, drop them
        while (!this.stock.isEmpty() && this.stock.getLast().isEmpty()) {
            this.stock.removeLast();
        }
        // load id of owner
        this.ownerId = pdc.get(this.ownerKey, UniqueIdDataType.INSTANCE);
    }

    public void saveData() {
        AbstractVillager merchant = this.getMerchant();
        PersistentDataContainer pdc = merchant.getPersistentDataContainer();
        this.stateLock.lock();
        try {
            // take an independent snapshot while holding the lock so concurrent
            // trades/menu interactions can't tear the serialized data; keep the
            // lock for the writes so two saves can't interleave
            int profitCount = 0;
            for (ItemStack stack : this.profit) {
                if (!stack.isEmpty()) {
                    profitCount++;
                }
            }
            org.bukkit.inventory.ItemStack[] profitSnapshot = new org.bukkit.inventory.ItemStack[profitCount];
            int profitIndex = 0;
            for (ItemStack stack : this.profit) {
                // empty profit entries are leftovers from old data, don't persist them
                if (!stack.isEmpty()) {
                    profitSnapshot[profitIndex++] = CraftItemStack.asBukkitCopy(stack);
                }
            }
            // trailing empty stacks are only padding added by the stock menu
            int stockSize = this.stock.size();
            while (stockSize > 0 && this.stock.get(stockSize - 1).isEmpty()) {
                stockSize--;
            }
            org.bukkit.inventory.ItemStack[] stockSnapshot = new org.bukkit.inventory.ItemStack[stockSize];
            for (int i = 0; i < stockSize; i++) {
                stockSnapshot[i] = CraftItemStack.asBukkitCopy(this.stock.get(i));
            }
            UUID ownerId = this.getOwnerId();
            // save profit stacks
            if (profitSnapshot.length > 0) {
                pdc.set(this.profitKey, ItemStackListDataType.INSTANCE, profitSnapshot);
            } else {
                pdc.remove(this.profitKey);
            }
            // save stock stacks
            if (stockSnapshot.length > 0) {
                pdc.set(this.stockKey, ItemStackListDataType.INSTANCE, stockSnapshot);
            } else {
                pdc.remove(this.stockKey);
            }
            // save id of owner
            if (ownerId != null) {
                pdc.set(this.ownerKey, UniqueIdDataType.INSTANCE, ownerId);
            } else {
                pdc.remove(this.ownerKey);
            }
        } finally {
            this.stateLock.unlock();
        }
    }

    public void markDirty() {
        this.onDirty.accept(this);
    }

    public void ensureLoaded() {
        this.getMerchant();
    }

    public UUID getMerchantId() {
        return this.merchantId;
    }

    public AbstractVillager getMerchant() {
        AbstractVillager merchant = this.merchant.get();
        if (merchant == null || !merchant.isValid()) {
            throw new IllegalStateException("Shop villager " + merchant + " (" + this.merchantId + ") is no longer valid");
        }
        return merchant;
    }

    public @Nullable AbstractVillager getMerchantIfValid() {
        AbstractVillager merchant = this.merchant.get();
        return merchant != null && merchant.isValid() ? merchant : null;
    }

    public boolean hasMerchantReference() {
        return this.merchant.get() != null;
    }

    public boolean isOwner(Entity entity) {
        UUID ownerId = this.getOwnerId();
        if (ownerId == null) {
            return true; // freely usable? Idk how this might happen
        }
        return ownerId.equals(entity.getUniqueId());
    }

    public @Nullable UUID getOwnerId() {
        synchronized (this) {
            return this.ownerId;
        }
    }

    public void setOwnerId(@Nullable UUID ownerId) {
        synchronized (this) {
            if (!Objects.equals(this.ownerId, ownerId)) {
                this.ownerId = ownerId;
                this.markDirty();
            }
        }
    }
}
