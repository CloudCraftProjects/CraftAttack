package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (01:08 30.10.21)

import dev.booky.cloudcore.util.BlockBBox;
import org.bukkit.Location;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.LinkedHashMap;
import java.util.Map;

// Can't be final because of object mapping
@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public class CaConfig {

    // I know this name sounds weird, but this is the actual plural of "status"
    // (according to wiktionary)
    private Map<String, Integer> statuses = new LinkedHashMap<>() {{
        put("Farming", 0x10a361);
        put("Building", 0x13f21e);
        put("Redstone", 0xe50d35);

        put("AFK", 0xd67f2f);
        put("LIVE", 0x920de5);
        put("REC", 0xfc2a0f);
    }};

    private EndConfig end = new EndConfig();

    @ConfigSerializable
    public static class EndConfig {

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

    public Map<String, Integer> getStatuses() {
        return statuses;
    }

    public EndConfig getEndConfig() {
        return this.end;
    }

    public SpawnConfig getSpawnConfig() {
        return this.spawn;
    }
}
