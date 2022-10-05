package dev.booky.craftattack;
// Created by booky10 in CraftAttack (14:51 01.03.21)

import dev.booky.craftattack.config.ConfigLoader;
import dev.booky.craftattack.utils.CaBoundingBox;
import dev.booky.craftattack.utils.CaConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class CaManager {

    // <gray>[<gradient:#aaffdd:#55eeee>CraftAttack</gradient>]</gray><space>
    private static final Component PREFIX = Component.text()
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
    private final NamespacedKey elytraDataKey;
    private final Plugin plugin;

    private final Path configPath;
    private CaConfig config;

    public CaManager(Plugin plugin, Path configDir) {
        this.elytraDataKey = new NamespacedKey(plugin, "elytra_data");
        this.plugin = plugin;
        this.configPath = configDir.resolve("config.yml");
    }

    public void updateConfig(Consumer<CaConfig> updater) {
        updater.accept(this.getConfig());
        this.saveConfig();
    }

    public void reloadConfig() {
        this.config = ConfigLoader.loadObject(this.configPath, CaConfig.class);
    }

    public void saveConfig() {
        ConfigLoader.saveObject(this.configPath, this.getConfig());
    }

    public void fail(Audience audience, String message) {
        audience.sendMessage(Identity.nil(), prefix(Component.text(message, NamedTextColor.RED)), MessageType.SYSTEM);
    }

    public void fail(Audience audience, Component component) {
        audience.sendMessage(Identity.nil(), prefix(component.color(NamedTextColor.RED)), MessageType.SYSTEM);
    }

    public void message(Audience audience, String message) {
        audience.sendMessage(Identity.nil(), prefix(Component.text(message, NamedTextColor.GREEN)), MessageType.SYSTEM);
    }

    public void message(Audience audience, Component component) {
        audience.sendMessage(Identity.nil(), PREFIX.append(component), MessageType.SYSTEM);
    }

    public Component prefix(String message) {
        return PREFIX.append(Component.text(message, NamedTextColor.GREEN));
    }

    public Component prefix(Component component) {
        return PREFIX.append(component);
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
        if (entity.getPersistentDataContainer().has(elytraDataKey, PersistentDataType.BYTE_ARRAY)) {
            return false;
        }

        ItemStack item = entity.getInventory().getChestplate();
        byte[] itemBytes = (item == null ? new byte[0] : item.serializeAsBytes());
        entity.getPersistentDataContainer().set(elytraDataKey, PersistentDataType.BYTE_ARRAY, itemBytes);

        ItemStack elytra = new ItemStack(Material.ELYTRA);
        elytra.editMeta(meta -> {
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
            meta.setUnbreakable(true);
        });

        entity.getInventory().setChestplate(elytra);
        return true;
    }

    public boolean removeElytra(HumanEntity entity) {
        byte[] itemBytes = entity.getPersistentDataContainer().get(elytraDataKey, PersistentDataType.BYTE_ARRAY);
        if (itemBytes == null) {
            return false;
        }

        ItemStack item = itemBytes.length == 0 ? new ItemStack(Material.AIR) : ItemStack.deserializeBytes(itemBytes);
        entity.getPersistentDataContainer().remove(elytraDataKey);
        entity.getInventory().setChestplate(item);
        return true;
    }

    public boolean hasElytra(PersistentDataHolder holder) {
        return holder.getPersistentDataContainer().has(elytraDataKey, PersistentDataType.BYTE_ARRAY);
    }

    public Map<UUID, BukkitTask> getTeleportRunnables() {
        return teleportRunnables;
    }

    public CaConfig getConfig() {
        return Objects.requireNonNull(config, "Config has not been loaded yet");
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
