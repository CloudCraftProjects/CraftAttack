package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (14:51 01.03.21)

import dev.booky.craftattack.CraftAttackMain;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static org.bukkit.Material.AIR;
import static org.bukkit.Material.ELYTRA;
import static org.bukkit.enchantments.Enchantment.BINDING_CURSE;
import static org.bukkit.enchantments.Enchantment.VANISHING_CURSE;
import static org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS;
import static org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE;
import static org.bukkit.persistence.PersistentDataType.BYTE_ARRAY;

public final class CraftAttackManager {

    private static final Component PREFIX = Component.text() // <gray>[<gradient:#aaffdd:#55eeee>CraftAttack</gradient>]</gray><space>
            .append(Component.text('[', NamedTextColor.GRAY))
            .append(Component.text('C', TextColor.color(0xaaffdd)))
            .append(Component.text('r', TextColor.color(0xa2fddf)))
            .append(Component.text('a', TextColor.color(0x9bfce0)))
            .append(Component.text('f', TextColor.color(0x93fae2)))
            .append(Component.text('t', TextColor.color(0x8bf9e3)))
            .append(Component.text('A', TextColor.color(0x83f7e5)))
            .append(Component.text('t', TextColor.color(0x7cf6e6)))
            .append(Component.text('t', TextColor.color(0x74f4e8)))
            .append(Component.text('a', TextColor.color(0x6cf3e9)))
            .append(Component.text('c', TextColor.color(0x64f1eb)))
            .append(Component.text('k', TextColor.color(0x5df0ec)))
            .append(Component.text(']', NamedTextColor.GRAY))
            .append(Component.space()).build();

    private final Map<UUID, BukkitTask> teleportRunnables = new HashMap<>();
    private final NamespacedKey elytraData;
    private final CraftAttackMain main;
    private World overworld;

    public CraftAttackManager(CraftAttackMain main) {
        this.elytraData = new NamespacedKey((this.main = main), "elytra_data");
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
        return isProtected(location.getWorld(), location.getX(), location.getY(), location.getZ(), entity);
    }

    public boolean isProtected(Block block, @Nullable HumanEntity entity) {
        return isProtected(block.getWorld(), block.getX() + 0.5d, block.getY() + 0.5d, block.getZ() + 0.5d, entity);
    }

    public boolean isProtected(World world, double x, double y, double z, @Nullable HumanEntity entity) {
        if (entity != null && entity.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        for (CaBoundingBox bbox : getConfig().getProtectedAreas()) {
            if (bbox.getWorld() != world) {
                continue;
            }
            if (!bbox.contains(x, y, z)) {
                continue;
            }
            return true;
        }
        return false;
    }

    public boolean inElytraBox(Location location) {
        if (getConfig().getSpawnConfig().getElytraBox() == null) {
            return false;
        }
        return getConfig().getSpawnConfig().getElytraBox().contains(location);
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

    public CaConfig getConfig() {
        return main.getCaConfig();
    }

    public World overworld() {
        return overworld;
    }

    public CraftAttackMain getMain() {
        return main;
    }
}
