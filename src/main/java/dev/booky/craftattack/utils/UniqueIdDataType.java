package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (23:45 26.10.2025)

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.UUID;

@NullMarked
public final class UniqueIdDataType implements PersistentDataType<long[], UUID> {

    public static final PersistentDataType<long[], UUID> INSTANCE = new UniqueIdDataType();

    private UniqueIdDataType() {
    }

    @Override
    public Class<long[]> getPrimitiveType() {
        return long[].class;
    }

    @Override
    public Class<UUID> getComplexType() {
        return UUID.class;
    }

    @Override
    public long[] toPrimitive(UUID complex, PersistentDataAdapterContext context) {
        return new long[]{complex.getMostSignificantBits(), complex.getLeastSignificantBits()};
    }

    @Override
    public UUID fromPrimitive(long[] primitive, PersistentDataAdapterContext context) {
        if (primitive.length != 2) {
            throw new IllegalStateException("Invalid long array for UUID: " + Arrays.toString(primitive));
        }
        return new UUID(primitive[0], primitive[1]);
    }
}
