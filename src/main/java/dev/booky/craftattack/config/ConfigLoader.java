package dev.booky.craftattack.config;
// Created by booky10 in ********** (12:55 27.06.22)

import dev.booky.craftattack.utils.CaBoundingBox;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.util.BoundingBox;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {

    private static final Map<Path, YamlConfigurationLoader> LOADER_CACHE = new HashMap<>();

    public static YamlConfigurationLoader createLoader(Path path) {
        return LOADER_CACHE.computeIfAbsent(path, $ -> YamlConfigurationLoader.builder().path(path)
                .defaultOptions(opts -> opts.serializers(builder -> builder
                        .register(NamespacedKey.class, NamespacedKeySerializer.INSTANCE)
                        .register(Location.class, LocationSerializer.INSTANCE)
                        .register(CaBoundingBox.class, CaBoundingBoxSerializer.INSTANCE)))
                .nodeStyle(NodeStyle.BLOCK).indent(2).build());
    }

    public static <T> T loadObject(Path path, Class<T> clazz) {
        try {
            YamlConfigurationLoader loader = createLoader(path);
            T obj;

            if (Files.exists(path)) {
                obj = loader.load().get(clazz);
            } else {
                try {
                    Constructor<T> ctor = clazz.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    obj = ctor.newInstance();
                } catch (ReflectiveOperationException exception) {
                    throw new RuntimeException(exception);
                }
            }

            CommentedConfigurationNode node = loader.createNode();
            node.set(clazz, obj);

            loader.save(node);
            return obj;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> void saveObject(Path path, T obj) {
        try {
            YamlConfigurationLoader loader = createLoader(path);
            CommentedConfigurationNode node = loader.createNode();
            node.set(obj.getClass(), obj);
            loader.save(node);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
