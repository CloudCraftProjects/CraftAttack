package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (23:07 26.10.2025)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.ItemStackListDataType;
import dev.booky.craftattack.utils.PlayerHeadUtil;
import dev.booky.craftattack.utils.UniqueIdDataType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
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
    private final NamespacedKey stockKey;
    private final NamespacedKey ownerKey;

    private final List<ItemStack> profit = new ArrayList<>();
    private final List<ItemStack> stock = new ArrayList<>();
    private @Nullable WeakReference<InventoryView> stockInventory;
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

    public static ItemStack createSpawnEgg(Plugin plugin) {
        NamespacedKey shopKey = new NamespacedKey(plugin, "shop");
        ItemStack stack = ItemStack.of(Material.VILLAGER_SPAWN_EGG);
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
        synchronized (this) {
            this.updateStock();
            if (this.isTradeStocked(recipe)) {
                this.consumeStock(recipe.getResult());
                this.addProfit(recipe);
                return true;
            }
            return false;
        }
    }

    public boolean isTradeStocked(MerchantRecipe recipe) {
        synchronized (this) {
            ItemStack result = recipe.getResult().clone();
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
    }

    public void consumeStock(ItemStack stack) {
        synchronized (this) {
            this.consumeStockFromOpenInventory(stack);
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
    }

    public void addProfit(MerchantRecipe recipe) {
        for (ItemStack ingredient : recipe.getIngredients()) {
            if (ShopMenu.isValidIngredient(ingredient)) {
                this.addProfit(ingredient);
            }
        }
    }

    public void addProfit(ItemStack profit) {
        synchronized (this) {
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
    }

    public boolean hasStock() {
        synchronized (this) {
            this.cleanStock();
            return !this.stock.isEmpty();
        }
    }

    public List<ItemStack> getStock() {
        synchronized (this) {
            return List.copyOf(this.stock);
        }
    }

    public void setStock(List<ItemStack> stock) {
        synchronized (this) {
            this.stock.clear();
            this.stock.addAll(stock);
            this.markDirty();
        }
    }

    public void setStock(@Nullable InventoryView view) {
        synchronized (this) {
            this.stockInventory = view != null ? new WeakReference<>(view) : null;
        }
    }

    public void updateStock() {
        this.updateStock(true);
    }

    private @Nullable Inventory getOpenStockInventory() {
        this.ensureLoaded();
        WeakReference<InventoryView> ref = this.stockInventory;
        if (ref == null) {
            return null;
        }
        InventoryView inv = ref.get();
        if (inv == null) {
            return null;
        }
        HumanEntity holder = inv.getPlayer();
        if (holder.getOpenInventory() != inv) {
            return null;
        }
        // ignore if not on correct region (folia only)
        if (!Bukkit.isOwnedByCurrentRegion(holder.getLocation())) {
            return null;
        }
        return inv.getTopInventory();
    }

    private void consumeStockFromOpenInventory(ItemStack stack) {
        Inventory inv = this.getOpenStockInventory();
        if (inv == null) {
            return; // no inventory set, ignore
        }
        stack = stack.clone();
        for (ItemStack stock : inv.getStorageContents()) {
            if (stock == null || !stack.isSimilar(stock)) {
                continue; // doesn't match
            }
            int newAmount = Math.max(0, stack.getAmount() - stock.getAmount());
            stock.subtract(stack.getAmount());
            stack.setAmount(newAmount);
            if (stack.isEmpty()) {
                break;
            }
        }
    }

    private void updateStock(boolean save) {
        Inventory inv = this.getOpenStockInventory();
        if (inv == null) {
            return; // no inventory set, ignore
        }
        this.stock.clear();
        for (ItemStack stack : inv.getStorageContents()) {
            if (stack == null || stack.isEmpty()
                    || stack.getPersistentDataContainer().has(PlayerHeadUtil.CUSTOM_HEAD_KEY)) {
                continue; // ignore custom heads (back/next buttons)
            }
            this.stock.add(stack.clone());
        }
        if (save) {
            this.markDirty();
        }
    }

    public void cleanStock() {
        synchronized (this) {
            boolean mut = this.stock.removeIf(ItemStack::isEmpty);
            if (mut) {
                this.markDirty();
            }
        }
    }

    public boolean hasProfit() {
        synchronized (this) {
            return !this.profit.isEmpty();
        }
    }

    public List<ItemStack> getProfit() {
        synchronized (this) {
            return List.copyOf(this.profit);
        }
    }

    public boolean dumpProfit(Inventory inventory) {
        synchronized (this) {
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
        ItemStack[] profitSnapshot;
        ItemStack[] stockSnapshot;
        UUID ownerSnapshot;
        synchronized (this) {
            this.updateStock(false);
            profitSnapshot = this.profit.toArray(new ItemStack[0]);
            stockSnapshot = this.stock.toArray(new ItemStack[0]);
            ownerSnapshot = this.ownerId;
        }
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
        if (ownerSnapshot != null) {
            pdc.set(this.ownerKey, UniqueIdDataType.INSTANCE, ownerSnapshot);
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

    public @Nullable AbstractVillager getMerchantIfValid() {
        AbstractVillager merchant = this.merchant.get();
        return merchant != null && merchant.isValid() ? merchant : null;
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
