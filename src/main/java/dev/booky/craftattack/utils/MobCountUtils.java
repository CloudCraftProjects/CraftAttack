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

    public static Map<Location, Integer> run(World world, EntityType type) {
        Class<? extends Entity> entityClass = Objects.requireNonNull(type.getEntityClass());
        Collection<? extends Entity> entities = world.getEntitiesByClass(entityClass);
        if (entities.isEmpty()) {
            return Map.of();
        }

        List<Entity> shuffledEntities = new ArrayList<>(entities);
        Collections.shuffle(shuffledEntities);

        Set<UUID> processedEntities = new HashSet<>(shuffledEntities.size());
        Map<Location, Integer> result = new HashMap<>();

        for (Entity entity : shuffledEntities) {
            if (!processedEntities.add(entity.getUniqueId())) {
                continue;
            }

            Collection<? extends Entity> foundEntities = entity.getWorld().getNearbyEntitiesByType(entityClass,
                    entity.getLocation(), SEARCH_RADIUS);
            for (Entity found : foundEntities) {
                processedEntities.add(found.getUniqueId());
            }

            result.put(entity.getLocation(), foundEntities.size());
        }

        return Collections.unmodifiableMap(result);
    }
}
