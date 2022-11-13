package dev.booky.craftattack.config;
// Created by booky10 in CraftAttack (21:22 13.11.22)

import dev.booky.craftattack.utils.CaBoundingBox;
import dev.booky.craftattack.utils.ProtectedArea;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public class ProtectedAreaSerializer implements TypeSerializer<ProtectedArea> {

    public static final ProtectedAreaSerializer INSTANCE = new ProtectedAreaSerializer();

    private ProtectedAreaSerializer() {
    }

    @Override
    public ProtectedArea deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.virtual()) {
            return null;
        }

        CaBoundingBox box = Objects.requireNonNull(node.get(CaBoundingBox.class));
        return new ProtectedArea(box);
    }

    @Override
    public void serialize(Type type, @Nullable ProtectedArea obj, ConfigurationNode node) throws SerializationException {
        node.set(obj == null ? null : obj.getBox());
    }
}
