package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (23:07 26.10.2025)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.ItemStackListDataType;
import dev.booky.craftattack.utils.UniqueIdDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
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

@NullMarked
public final class ShopVillager {

    private final WeakReference<AbstractVillager> merchant;
    private final UUID merchantId;
    private final Consumer<ShopVillager> onDirty;

    private final NamespacedKey profitKey;
    private final List<ItemStack> profit = new ArrayList<>();

    private final NamespacedKey ownerKey;
    private @Nullable UUID ownerId;

    public ShopVillager(CaManager manager, AbstractVillager merchant, Consumer<ShopVillager> onDirty) {
        this.merchant = new WeakReference<>(merchant);
        this.merchantId = merchant.getUniqueId();
        this.onDirty = onDirty;

        this.profitKey = new NamespacedKey(manager.getPlugin(), "shop/profit");
        this.ownerKey = new NamespacedKey(manager.getPlugin(), "shop/owner");

        this.loadData();
    }

    public void addProfit(ItemStack profit) {
        this.getMerchant(); // ensure merchant still exists
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

    public boolean hasProfit() {
        return !this.profit.isEmpty();
    }

    public List<ItemStack> getProfit() {
        return this.profit;
    }

    public boolean dumpProfit(Inventory inventory) {
        this.getMerchant(); // ensure merchant still exists
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
        ItemStack[] stacks = pdc.get(this.profitKey, ItemStackListDataType.INSTANCE);
        if (stacks != null && stacks.length > 0) {
            this.profit.addAll(List.of(stacks));
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
