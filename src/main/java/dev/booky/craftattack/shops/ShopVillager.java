package dev.booky.craftattack.shops;
// Created by booky10 in CraftAttack (23:07 26.10.2025)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.ItemStackListDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@NullMarked
public final class ShopVillager {

    private final WeakReference<AbstractVillager> merchant;
    private final UUID merchantId;

    private final NamespacedKey profitKey;
    private final Consumer<ShopVillager> onDirty;

    private final List<ItemStack> profit = new ArrayList<>();

    public ShopVillager(CaManager manager, AbstractVillager merchant, Consumer<ShopVillager> onDirty) {
        this.merchant = new WeakReference<>(merchant);
        this.merchantId = merchant.getUniqueId();

        this.profitKey = new NamespacedKey(manager.getPlugin(), "shop/profit");
        this.onDirty = onDirty;
        this.loadData();
    }

    public void addProfit(ItemStack profit) {
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

    public boolean dumpProfit(Inventory inventory) {
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
            break;
        }
        if (mut) {
            this.markDirty();
        }
        return ret; // success if no errors occur
    }

    private void loadData() {
        ItemStack[] stacks = this.getMerchant().getPersistentDataContainer().get(this.profitKey, ItemStackListDataType.INSTANCE);
        if (stacks != null && stacks.length > 0) {
            this.profit.addAll(List.of(stacks));
        }
    }

    public void saveData() {
        this.getMerchant().getPersistentDataContainer().set(this.profitKey, ItemStackListDataType.INSTANCE,
                this.profit.toArray(new ItemStack[0]));
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
}
