package dev.booky.craftattack.config;
// Created by booky10 in CraftAttack (13:15 05.10.22)

import dev.booky.craftattack.utils.CaBoundingBox;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class CaBoundingBoxSerializer implements TypeSerializer<CaBoundingBox> {

    public static final CaBoundingBoxSerializer INSTANCE = new CaBoundingBoxSerializer();

    private CaBoundingBoxSerializer() {
    }

    @Override
    public CaBoundingBox deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.virtual()) {
            return null;
        }

        Location corner1 = node.node("corner1").get(Location.class);
        assert corner1 != null : "No first corner position specified";

        Location corner2 = node.node("corner2").get(Location.class);
        assert corner2 != null : "No second corner position specified";

        return new CaBoundingBox(corner1, corner2);
    }

    @Override
    public void serialize(Type type, @Nullable CaBoundingBox obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }

        node.node("corner1").set(obj.getMinPos());
        node.node("corner2").set(obj.getMaxPos());
    }
}
