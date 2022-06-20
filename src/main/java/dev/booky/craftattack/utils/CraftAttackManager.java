package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (14:51 01.03.21)

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static org.bukkit.Material.AIR;
import static org.bukkit.Material.ELYTRA;
import static org.bukkit.enchantments.Enchantment.BINDING_CURSE;
import static org.bukkit.enchantments.Enchantment.VANISHING_CURSE;
import static org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS;
import static org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE;
import static org.bukkit.persistence.PersistentDataType.BYTE_ARRAY;

public final class CraftAttackManager {

    private static final Component PREFIX = text()
            .append(text('[', GRAY))
            .append(text('C', WHITE, BOLD))
            .append(text('A', AQUA, BOLD))
            .append(text(']', GRAY))
            .append(space()).build();

    private final Map<UUID, BukkitTask> teleportRunnables = new HashMap<>();
    private final CraftAttackConfig config;
    private final NamespacedKey elytraData;
    private final Plugin plugin;
    private World overworld;

    public CraftAttackManager(CraftAttackConfig config, Plugin plugin) {
        this.elytraData = new NamespacedKey((this.plugin = plugin), "elytra_data");
        this.config = config;
    }

    public void fail(Audience audience, String message) {
        audience.sendMessage(Identity.nil(), prefix(text(message, RED)), MessageType.SYSTEM);
    }

    public void fail(Audience audience, Component component) {
        audience.sendMessage(Identity.nil(), prefix(component.color(RED)), MessageType.SYSTEM);
    }

    public void message(Audience audience, String message) {
        audience.sendMessage(Identity.nil(), prefix(text(message, GREEN)), MessageType.SYSTEM);
    }

    public void message(Audience audience, Component component) {
        audience.sendMessage(Identity.nil(), PREFIX.append(component), MessageType.SYSTEM);
    }

    public Component prefix(String message) {
        return PREFIX.append(text(message, GREEN));
    }

    public Component prefix(Component component) {
        return PREFIX.append(component);
    }

    public CraftAttackManager loadOverworld() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                overworld = world;
                return this;
            }
        }

        throw new IllegalStateException("No overworld could be found!");
    }

    public boolean isProtected(Location location, @Nullable HumanEntity entity) {
        if (entity != null && entity.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        if (config.protectedArea().contains(location.toVector())) {
            return true;
        }

        return isInRadius(location, config.spawnLocation(), config.spawnRadiusSquared()) ||
                isInRadius(location, config.endLocation(), config.endRadiusSquared());
    }

    public boolean isInRadius(Location source, Location target, int squaredRadius) {
        return target != null && target.getWorld() == source.getWorld() && distanceXZSquared(source, target) <= squaredRadius;
    }

    public boolean isInSpawn(Location location, @Nullable HumanEntity entity) {
        return isInRadius(location, entity, config.spawnLocation(), config.spawnRadiusSquared());
    }

    public boolean isInEnd(Location location, @Nullable HumanEntity entity) {
        return isInRadius(location, entity, config.endLocation(), config.endRadiusSquared());
    }

    public boolean isInRadius(Location target, @Nullable HumanEntity entity, Location source, int radiusSquared) {
        if (source == null || radiusSquared <= 0) {
            return false;
        }

        if (entity != null && entity.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        if (target.getWorld() != source.getWorld()) {
            return false;
        }

        return distanceXZSquared(source, target) <= radiusSquared;
    }

    private double distanceXZSquared(Location loc1, Location loc2) {
        return NumberConversions.square(loc1.getX() - loc2.getX())
                + NumberConversions.square(loc1.getZ() - loc2.getZ());
    }

    public boolean giveElytra(HumanEntity entity) {
        if (entity.getPersistentDataContainer().has(elytraData, BYTE_ARRAY)) {
            return false;
        }

        ItemStack item = entity.getInventory().getChestplate();
        byte[] itemBytes = (item == null ? new byte[0] : item.serializeAsBytes());
        entity.getPersistentDataContainer().set(elytraData, BYTE_ARRAY, itemBytes);

        ItemStack elytra = new ItemStack(ELYTRA);
        elytra.editMeta(meta -> {
            meta.addEnchant(VANISHING_CURSE, 1, true);
            meta.addEnchant(BINDING_CURSE, 1, true);
            meta.addItemFlags(HIDE_UNBREAKABLE, HIDE_ENCHANTS);
            meta.setUnbreakable(true);
        });

        entity.getInventory().setChestplate(elytra);
        return true;
    }

    public boolean removeElytra(HumanEntity entity) {
        byte[] itemBytes = entity.getPersistentDataContainer().get(elytraData, BYTE_ARRAY);
        if (itemBytes == null) return false;

        ItemStack item = itemBytes.length == 0 ? new ItemStack(AIR) : ItemStack.deserializeBytes(itemBytes);
        entity.getPersistentDataContainer().remove(elytraData);
        entity.getInventory().setChestplate(item);
        return true;
    }

    public boolean hasElytra(PersistentDataHolder holder) {
        return holder.getPersistentDataContainer().has(elytraData, BYTE_ARRAY);
    }

    public Map<UUID, BukkitTask> teleportRunnables() {
        return teleportRunnables;
    }

    public CraftAttackConfig config() {
        return config;
    }

    public World overworld() {
        return overworld;
    }

    public Plugin plugin() {
        return plugin;
    }
}
