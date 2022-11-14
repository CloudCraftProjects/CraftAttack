package dev.booky.craftattack.config;
// Created by booky10 in CraftAttack (21:22 13.11.22)

import dev.booky.craftattack.utils.CaBoundingBox;
import dev.booky.craftattack.utils.ProtectedArea;
import dev.booky.craftattack.utils.ProtectionFlag;
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
        ProtectionFlag[] flags = ProtectionFlag.values();

        ConfigurationNode flagsNode = node.node("flags");
        if (!flagsNode.virtual()) {
            // my enum serializer doesn't work here and i dont know why
            flags = Objects.requireNonNull(flagsNode.getList(String.class)).stream()
                    .map(ProtectionFlag::valueOf).toArray(ProtectionFlag[]::new);
        }

        return new ProtectedArea(box, flags);
    }

    @Override
    public void serialize(Type type, @Nullable ProtectedArea obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }

        node.set(obj.getBox());
        node.node("flags").set(obj.getFlags().stream()
                .map(ProtectionFlag::name).toList());
    }
}
