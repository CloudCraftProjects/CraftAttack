package tk.booky.craftattack.utils;
// Created by booky10 in CraftAttack (01:08 30.10.21)

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CraftAttackConfig {

    private final File configurationFile;
    private int endRadiusSquared = 0, spawnRadiusSquared = 0, endRadius = 0, spawnRadius = 0;
    private Location endLocation, spawnLocation;
    private boolean endActivated = true;

    public CraftAttackConfig(File configurationFile) {
        this.configurationFile = configurationFile;
    }

    public CraftAttackConfig reloadConfiguration() {
        if (!configurationFile.exists()) {
            return saveConfiguration();
        } else {
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(configurationFile);

            endActivated = configuration.getBoolean("end.activated", endActivated);
            endLocation = configuration.getLocation("end.location", endLocation);
            endRadius = configuration.getInt("end.radius", endRadius);

            spawnLocation = configuration.getLocation("spawn.location", spawnLocation);
            spawnRadius = configuration.getInt("spawn.radius", spawnRadius);

            spawnRadiusSquared = spawnRadius * spawnRadius;
            endRadiusSquared = endRadius * endRadius;
            return this;
        }
    }

    public CraftAttackConfig saveConfiguration() {
        try {
            FileConfiguration configuration = new YamlConfiguration();

            configuration.set("end.activated", endActivated);
            configuration.set("end.location", endLocation);
            configuration.set("end.radius", endRadius);

            configuration.set("spawn.location", spawnLocation);
            configuration.set("spawn.radius", spawnRadius);

            configuration.save(configurationFile);
            return this;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public File configurationFile() {
        return configurationFile;
    }

    public int endRadiusSquared() {
        return endRadiusSquared;
    }

    public void endRadiusSquared(int endRadiusSquared) {
        this.endRadiusSquared = endRadiusSquared;
        saveConfiguration();
    }

    public int spawnRadiusSquared() {
        return spawnRadiusSquared;
    }

    public void spawnRadiusSquared(int spawnRadiusSquared) {
        this.spawnRadiusSquared = spawnRadiusSquared;
        saveConfiguration();
    }

    public int endRadius() {
        return endRadius;
    }

    public void endRadius(int endRadius) {
        this.endRadius = endRadius;
        saveConfiguration();
    }

    public int spawnRadius() {
        return spawnRadius;
    }

    public void spawnRadius(int spawnRadius) {
        this.spawnRadius = spawnRadius;
        saveConfiguration();
    }

    public Location endLocation() {
        return endLocation;
    }

    public void endLocation(Location endLocation) {
        this.endLocation = endLocation;
        saveConfiguration();
    }

    public Location spawnLocation() {
        return spawnLocation;
    }

    public void spawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
        saveConfiguration();
    }

    public boolean endActivated() {
        return endActivated;
    }

    public void endActivated(boolean endActivated) {
        this.endActivated = endActivated;
        saveConfiguration();
    }
}
