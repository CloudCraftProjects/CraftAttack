package dev.booky.craftattack.config;
// Created by booky10 in CraftAttack (13:23 05.10.22)

import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public class NamespacedKeySerializer implements TypeSerializer<NamespacedKey> {

    public static final NamespacedKeySerializer INSTANCE = new NamespacedKeySerializer();

    private NamespacedKeySerializer() {
    }

    @Override
    public NamespacedKey deserialize(Type type, ConfigurationNode node) throws SerializationException {
        return node.virtual() ? null : NamespacedKey.fromString(Objects.requireNonNull(node.getString()));
    }

    @Override
    public void serialize(Type type, @Nullable NamespacedKey obj, ConfigurationNode node) throws SerializationException {
        node.set(obj == null ? null : obj.asString());
    }
}
