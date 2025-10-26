package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (23:11 26.10.2025)

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ItemStackListDataType implements PersistentDataType<byte[], ItemStack[]> {

    public static final PersistentDataType<byte[], ItemStack[]> INSTANCE = new ItemStackListDataType();

    private ItemStackListDataType() {
    }

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<ItemStack[]> getComplexType() {
        return ItemStack[].class;
    }

    @Override
    public byte[] toPrimitive(ItemStack[] complex, PersistentDataAdapterContext context) {
        return ItemStack.serializeItemsAsBytes(complex);
    }

    @Override
    public ItemStack[] fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
        return ItemStack.deserializeItemsFromBytes(primitive);
    }
}
