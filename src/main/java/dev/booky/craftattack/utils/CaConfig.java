package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (01:08 30.10.21)

import org.bukkit.Location;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

// Can't be final because of object mapping
@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public class CaConfig {

    private Set<ProtectedArea> protectedAreas = new LinkedHashSet<>();
    private Set<Location> launchPlates = new LinkedHashSet<>();

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
        private CaBoundingBox elytraBox;

        public Location getWarpLocation() {
            return this.warpLocation;
        }

        public CaBoundingBox getElytraBox() {
            return this.elytraBox;
        }

        public void setWarpLocation(Location warpLocation) {
            this.warpLocation = warpLocation;
        }

        public void setElytraBox(CaBoundingBox elytraBox) {
            this.elytraBox = elytraBox;
        }
    }

    public Set<ProtectedArea> getProtectedAreas() {
        return this.protectedAreas;
    }

    public Set<Location> getLaunchPlates() {
        return launchPlates;
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
