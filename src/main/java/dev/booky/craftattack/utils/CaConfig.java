package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (01:08 30.10.21)

import dev.booky.cloudcore.util.BlockBBox;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Can't be final because of object mapping
@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public class CaConfig {

    private Map<String, TagEntry> tags = new LinkedHashMap<>() {{
        put("Farming", new TagEntry(null, "<c:#10a361>Farming"));
        put("Building", new TagEntry(null, "<c:#13f21e>Building"));
        put("Redstone", new TagEntry(null, "<c:#e50d35>Redstone"));

        put("AFK", new TagEntry(null, "<c:#d67f2f>AFK"));
        put("LIVE", new TagEntry(null, "<c:#920de5>LIVE"));
        put("REC", new TagEntry(null, "<c:#fc2a0f>REC"));
    }};

    private String tagServerContext = "craftattack";

    @ConfigSerializable
    public static final class TagEntry {

        private @Nullable UUID creator;
        private String display = "";

        public TagEntry() {
        }

        public TagEntry(@Nullable UUID creator, String display) {
            this.creator = creator;
            this.display = display;
        }

        public @Nullable UUID getCreator() {
            return this.creator;
        }

        public String getDisplay() {
            return this.display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }
    }

    private FeaturesConfig features = new FeaturesConfig();

    @ConfigSerializable
    public static class FeaturesConfig {

        private boolean sitting = true;
        private boolean creeperNoBlockDamage = true;
        private double creeperDamageMultiplier = 0.5d;

        public boolean isSitting() {
            return this.sitting;
        }

        public boolean isCreeperNoBlockDamage() {
            return this.creeperNoBlockDamage;
        }

        public double getCreeperDamageMultiplier() {
            return this.creeperDamageMultiplier;
        }
    }

    private Map<Key, DimensionConfig> dimensions = new ConcurrentHashMap<>();

    @ConfigSerializable
    public static class DimensionConfig {

        private Location warpLocation;
        private boolean activated = true;

        public Location getWarpLocation() {
            return this.warpLocation;
        }

        public boolean isActivated() {
            return this.activated;
        }

        public void setWarpLocation(Location warpLocation) {
            this.warpLocation = warpLocation;
        }

        public void setActivated(boolean activated) {
            this.activated = activated;
        }
    }

    private SpawnConfig spawn = new SpawnConfig();

    @ConfigSerializable
    public static class SpawnConfig {

        private Location warpLocation;
        private BlockBBox elytraBox;

        private boolean elytraOnLaunch = true;
        private boolean elytraNoDamage = true;
        private int elytraBoosts = 5;
        private int elytraBoostDuration = 2; // seconds

        public Location getWarpLocation() {
            return this.warpLocation;
        }

        public BlockBBox getElytraBox() {
            return this.elytraBox;
        }

        public void setWarpLocation(Location warpLocation) {
            this.warpLocation = warpLocation;
        }

        public void setElytraBox(BlockBBox elytraBox) {
            this.elytraBox = elytraBox;
        }

        public boolean isElytraOnLaunch() {
            return this.elytraOnLaunch;
        }

        public boolean isElytraNoDamage() {
            return this.elytraNoDamage;
        }

        public int getElytraBoosts() {
            return this.elytraBoosts;
        }

        public void setElytraBoosts(int elytraBoosts) {
            this.elytraBoosts = elytraBoosts;
        }

        public int getElytraBoostDuration() {
            return this.elytraBoostDuration;
        }

        public void setElytraBoostDuration(int elytraBoostDuration) {
            this.elytraBoostDuration = elytraBoostDuration;
        }
    }

    private List<MineStatEntry> mineStats = List.of();

    @ConfigSerializable
    public static final class MineStatEntry {

        private String objectiveName;
        private Set<NamespacedKey> blocks = Set.of();
        private boolean preventAbuse = true;

        private MineStatEntry() {
        }

        public boolean hasBlock(Block block) {
            return this.hasBlock(block.getType());
        }

        public boolean hasBlock(Material material) {
            return this.blocks.contains(material.getKey());
        }

        public String getObjectiveName() {
            return this.objectiveName;
        }

        public boolean isPreventAbuse() {
            return this.preventAbuse;
        }
    }

    private Map<BlockPosition, String> warpPlates = new HashMap<>();

    private List<LeaderboardConfig> leaderboards = new ArrayList<>();

    @ConfigSerializable
    public static final class LeaderboardConfig {

        private String objective;
        private UUID entityId;
        private int entries = 10;
        private Component wrapper = Component.text("");

        public String getObjective() {
            return this.objective;
        }

        public UUID getEntityId() {
            return this.entityId;
        }

        public int getEntries() {
            return this.entries;
        }

        public Component getWrapper() {
            return this.wrapper;
        }
    }

    public List<LeaderboardConfig> getLeaderboards() {
        return this.leaderboards;
    }

    public @Nullable String getWarpPlateTarget(BlockPosition pos) {
        return this.warpPlates.get(pos);
    }

    public Map<String, TagEntry> getTags() {
        return this.tags;
    }

    public String getTagServerContext() {
        return this.tagServerContext;
    }

    public FeaturesConfig getFeatures() {
        return this.features;
    }

    public DimensionConfig getDimensionConfig(Key dimension) {
        return this.dimensions.computeIfAbsent(dimension, __ -> new DimensionConfig());
    }

    public SpawnConfig getSpawnConfig() {
        return this.spawn;
    }

    public List<MineStatEntry> getMineStats() {
        return this.mineStats;
    }
}
