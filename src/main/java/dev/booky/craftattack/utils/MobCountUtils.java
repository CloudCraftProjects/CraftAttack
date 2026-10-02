package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (20:56 30.10.22)

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class MobCountUtils {

    private static final int SEARCH_RADIUS = Integer.getInteger("ca.mobcount.search_radius", 48);

    private MobCountUtils() {
    }

    private static long bucketKey(int bucketX, int bucketZ) {
        // pack two ints into a long
        return (long) bucketX << 32 | bucketZ & 0xFFFFFFFFL;
    }

    private static boolean isWithinRadius(Location first, Location second) {
        return Math.abs(first.getX() - second.getX()) <= SEARCH_RADIUS
                && Math.abs(first.getY() - second.getY()) <= SEARCH_RADIUS
                && Math.abs(first.getZ() - second.getZ()) <= SEARCH_RADIUS;
    }

    public static Map<Location, Integer> run(World world, EntityType type) {
        Class<? extends Entity> entityClass = Objects.requireNonNull(type.getEntityClass());
        Collection<? extends Entity> entities = world.getEntitiesByClass(entityClass);
        if (entities.isEmpty()) {
            return Map.of();
        }

        List<Entity> shuffledEntities = new ArrayList<>(entities);
        Collections.shuffle(shuffledEntities);

        // sort entities into region "buckets" and do distance checks ourselves;
        // using e.g. Entity#getNearbyEntities isn't thread-safe on folia
        Map<Long, List<Entity>> buckets = new HashMap<>();
        for (Entity entity : shuffledEntities) {
            Location location = entity.getLocation();
            long key = bucketKey(Math.floorDiv(location.getBlockX(), SEARCH_RADIUS),
                    Math.floorDiv(location.getBlockZ(), SEARCH_RADIUS));
            buckets.computeIfAbsent(key, __ -> new ArrayList<>()).add(entity);
        }

        Set<UUID> processedEntities = new HashSet<>(shuffledEntities.size());
        Map<Location, Integer> result = new HashMap<>();

        for (Entity entity : shuffledEntities) {
            if (!processedEntities.add(entity.getUniqueId())) {
                continue; // already part of another cluster
            }

            Location location = entity.getLocation();
            int bucketX = Math.floorDiv(location.getBlockX(), SEARCH_RADIUS);
            int bucketZ = Math.floorDiv(location.getBlockZ(), SEARCH_RADIUS);

            // search neighboring buckets
            int count = 0;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    long bucketKey = bucketKey(bucketX + x, bucketZ + z);
                    List<Entity> bucket = buckets.getOrDefault(bucketKey, List.of());
                    for (Entity other : bucket) {
                        // still do distance checks, an entity being in a neighboring bucket
                        // doesn't guarantee it's also near ourselves
                        if (isWithinRadius(location, other.getLocation())) {
                            count++;
                            processedEntities.add(other.getUniqueId());
                        }
                    }
                }
            }

            result.put(location, count);
        }

        return Collections.unmodifiableMap(result);
    }
}
