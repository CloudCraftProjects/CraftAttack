package dev.booky.craftattack;
// Created by booky10 in CraftAttack (14:51 01.03.21)

import dev.booky.craftattack.config.ConfigLoader;
import dev.booky.craftattack.utils.CaBoundingBox;
import dev.booky.craftattack.utils.CaConfig;
import dev.booky.craftattack.utils.ProtectedArea;
import dev.booky.craftattack.utils.TpResult;
import dev.booky.craftattack.utils.TranslationLoader;
import io.papermc.paper.entity.RelativeTeleportFlag;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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

    private final Map<UUID, CompletableFuture<TpResult>> teleports = new HashMap<>();
    private final NamespacedKey elytraDataKey;
    private final Plugin plugin;

    private final Object2LongMap<UUID> lastBoost = new Object2LongOpenHashMap<>() {{
        this.defaultReturnValue(0);
    }};
    private final TranslationLoader i18n;

    private final Path configPath;
    private CaConfig config;

    public CaManager(Plugin plugin, Path configDir) {
        this.elytraDataKey = new NamespacedKey(plugin, "elytra_data");
        this.plugin = plugin;
        this.configPath = configDir.resolve("config.yml");

        this.i18n = new TranslationLoader(plugin);
        this.i18n.load();
    }

    public static Component getPrefix() {
        return PREFIX;
    }

    public CompletableFuture<TpResult> teleportRequest(Player player, Location target) {
        if (this.teleports.containsKey(player.getUniqueId())) {
            player.sendMessage(getPrefix().append(Component.translatable("ca.teleport.already", NamedTextColor.RED)));
            return CompletableFuture.completedFuture(TpResult.ALREADY_TELEPORTING);
        }

        if (target == null) {
            target = this.getConfig().getSpawnConfig().getWarpLocation();
            if (target == null) {
                target = player.getWorld().getSpawnLocation();
            }
            player.sendMessage(getPrefix().append(Component.translatable("ca.teleport.spawn-warning", NamedTextColor.RED)));
        }

        if (player.getAllowFlight()) {
            player.sendMessage(getPrefix().append(Component.translatable("ca.teleport.teleporting", NamedTextColor.GRAY)));

            Location finalTarget = target;
            return target.getWorld().getChunkAtAsync(target, true).thenApply(chunk -> {
                finalTarget.setYaw(player.getLocation().getYaw());
                finalTarget.setPitch(player.getLocation().getPitch());
                player.teleport(finalTarget, PlayerTeleportEvent.TeleportCause.COMMAND, true,
                        true, RelativeTeleportFlag.YAW, RelativeTeleportFlag.PITCH);
                return TpResult.SUCCESSFUL;
            });
        }

        player.sendMessage(getPrefix().append(Component.translatable("ca.teleport.please-wait", NamedTextColor.GRAY)));
        CompletableFuture<TpResult> future = new CompletableFuture<>();

        Location finalTarget = target;
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (future.isDone()) {
                return;
            }

            CaManager.this.teleports.remove(player.getUniqueId());
            player.sendMessage(getPrefix().append(Component.translatable("ca.teleport.teleporting", NamedTextColor.GRAY)));

            finalTarget.getWorld().getChunkAtAsync(finalTarget, true).thenAccept(chunk -> {
                finalTarget.setYaw(player.getLocation().getYaw());
                finalTarget.setPitch(player.getLocation().getPitch());
                player.teleport(finalTarget, PlayerTeleportEvent.TeleportCause.COMMAND, true,
                        true, RelativeTeleportFlag.YAW, RelativeTeleportFlag.PITCH);
                future.complete(TpResult.SUCCESSFUL);
            });
        }, 5 * 20);

        teleports.put(player.getUniqueId(), future);
        return future;
    }

    public void cancelTeleport(Player player, TpResult result) {
        CompletableFuture<TpResult> future = this.teleports.remove(player.getUniqueId());
        if (future == null) {
            return;
        }

        if (player.isOnline()) {
            player.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.teleport.moved", NamedTextColor.RED)));
        }
        future.complete(result);
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

    public boolean isProtected(Location location, @Nullable HumanEntity entity) {
        return isProtected(location.getWorld(), location.getX(), location.getY(), location.getZ(), entity);
    }

    public boolean isProtected(Block block, @Nullable HumanEntity entity) {
        return isProtected(block.getWorld(), block.getX() + 0.5d, block.getY() + 0.5d, block.getZ() + 0.5d, entity);
    }

    public boolean isProtected(World world, double x, double y, double z, @Nullable HumanEntity entity) {
        Set<Location> launchPlates = this.getConfig().getLaunchPlates();
        if (!launchPlates.isEmpty()) {
            int floorX = NumberConversions.floor(x);
            int floorY = NumberConversions.floor(y);
            int floorZ = NumberConversions.floor(z);

            for (Location plate : launchPlates) {
                if (plate.getX() != floorX) {
                    continue;
                }
                if (plate.getY() != floorY) {
                    // Prevents people breaking the block below the launch plate.
                    // Cancelling the physics event didn't work.
                    if (plate.getY() - 1 != floorY) {
                        continue;
                    }
                }
                if (plate.getZ() != floorZ) {
                    continue;
                }
                return true;
            }
        }

        if (entity != null && entity.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        for (ProtectedArea area : this.getConfig().getProtectedAreas()) {
            CaBoundingBox box = area.getBox();
            if (box.getWorld() != world) {
                continue;
            }
            if (!box.contains(x, y, z)) {
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
        if (this.hasElytra(entity)) {
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
            meta.displayName(Component.translatable(Material.ELYTRA.translationKey(), NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
        });

        entity.getInventory().setChestplate(elytra);
        return true;
    }

    public boolean removeElytra(HumanEntity entity) {
        if (!this.hasElytra(entity)) {
            return false;
        }

        byte[] itemBytes = entity.getPersistentDataContainer().get(elytraDataKey, PersistentDataType.BYTE_ARRAY);
        Objects.requireNonNull(itemBytes, () -> "Invalid elytra data set for key " + elytraDataKey);

        ItemStack item = itemBytes.length == 0 ? new ItemStack(Material.AIR) : ItemStack.deserializeBytes(itemBytes);
        entity.getPersistentDataContainer().remove(elytraDataKey);
        entity.getInventory().setChestplate(item);
        return true;
    }

    public boolean hasElytra(PersistentDataHolder holder) {
        return holder.getPersistentDataContainer().has(elytraDataKey, PersistentDataType.BYTE_ARRAY);
    }

    public boolean noBoostSince(Player player, long millis) {
        return System.currentTimeMillis() - this.getLastBoost(player) > millis;
    }

    public void setLastBoost(Player player) {
        this.lastBoost.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public long getLastBoost(Player player) {
        return this.lastBoost.getLong(player.getUniqueId());
    }

    public TranslationLoader getI18n() {
        return i18n;
    }

    public CaConfig getConfig() {
        return Objects.requireNonNull(config, "Config has not been loaded yet");
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
