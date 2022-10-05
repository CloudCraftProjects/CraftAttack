package dev.booky.craftattack.config;
// Created by booky10 in CraftAttack (13:15 05.10.22)

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public class LocationSerializer implements TypeSerializer<Location> {

    public static final LocationSerializer INSTANCE = new LocationSerializer();

    private LocationSerializer() {
    }

    @Override
    public Location deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.virtual()) {
            return null;
        }

        NamespacedKey worldKey = Objects.requireNonNull(node.node("world").get(NamespacedKey.class), "No world specified");
        World world = Objects.requireNonNull(Bukkit.getWorld(worldKey), "World with key '" + worldKey + "' not found");

        double x = node.node("x").getDouble(0d);
        double y = node.node("y").getDouble(0d);
        double z = node.node("z").getDouble(0d);
        float yaw = node.node("yaw").getFloat(0f);
        float pitch = node.node("pitch").getFloat(0f);
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public void serialize(Type type, @Nullable Location obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }

        assert obj.isWorldLoaded() : "Specified world not found/loaded";

        node.node("world").set(obj.getWorld().getKey());
        node.node("x").set(obj.getX());
        node.node("y").set(obj.getY());
        node.node("z").set(obj.getZ());

        if (obj.getYaw() != 0f || obj.getPitch() != 0f) {
            node.node("yaw").set(obj.getYaw());
            node.node("pitch").set(obj.getPitch());
        }
    }
}
