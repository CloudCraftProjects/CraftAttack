package dev.booky.craftattack;
// Created by booky10 in CraftAttack (14:51 01.03.21)

import dev.booky.cloudcore.config.ConfigurateLoader;
import dev.booky.cloudcore.util.BlockBBox;
import dev.booky.craftattack.menu.impl.MenuManager;
import dev.booky.craftattack.utils.CaConfig;
import dev.booky.craftattack.utils.LeaderboardTasks;
import dev.booky.craftattack.utils.TpResult;
import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static dev.booky.cloudcore.config.ConfigurateLoader.yamlLoader;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public final class CaManager {

    private static final Component PREFIX = empty()
            .append(miniMessage().deserialize("<gray>[<gradient:#aaffdd:#55eeee>CraftAttack</gradient>] </gray>"))
            .compact();
    private static final ConfigurateLoader<?, ?> CONFIG_LOADER = yamlLoader().withAllDefaultSerializers().build();

    private final NamespacedKey elytraDataKey;
    private final NamespacedKey elytraBoostsKey;

    private final MenuManager menus = new MenuManager();
    private final LeaderboardTasks leaderboardTasks = new LeaderboardTasks(this);
    private final Map<UUID, CompletableFuture<TpResult>> teleports = new HashMap<>();
    private final Plugin plugin;

    private final Path configPath;
    private CaConfig config;

    public CaManager(Plugin plugin, Path configDir) {
        this.elytraDataKey = new NamespacedKey(plugin, "elytra_data");
        this.elytraBoostsKey = new NamespacedKey(plugin, "elytra_boosts");

        this.plugin = plugin;
        this.configPath = configDir.resolve("config.yml");
    }

    private static ItemStack buildElytraStack() {
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        elytra.editMeta(meta -> {
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);

            meta.setUnbreakable(true);
            meta.displayName(translatable(Material.ELYTRA.translationKey(), NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
        });
        return elytra;
    }

    public static Component getPrefix() {
        return PREFIX;
    }

    public CompletableFuture<TpResult> teleportRequest(Player player, Location target) {
        if (this.teleports.containsKey(player.getUniqueId())) {
            player.sendMessage(getPrefix().append(translatable("ca.teleport.already", NamedTextColor.RED)));
            return CompletableFuture.completedFuture(TpResult.ALREADY_TELEPORTING);
        }

        if (target == null) {
            target = this.getConfig().getSpawnConfig().getWarpLocation();
            if (target == null) {
                target = player.getWorld().getSpawnLocation();
            }
            player.sendMessage(getPrefix().append(translatable("ca.teleport.spawn-warning", NamedTextColor.RED)));
        }

        if (player.getAllowFlight()) {
            player.sendMessage(getPrefix().append(translatable("ca.teleport.teleporting", NamedTextColor.GRAY)));

            Location finalTarget = target;
            return target.getWorld().getChunkAtAsync(target, true).thenApply(chunk -> {
                finalTarget.setYaw(player.getLocation().getYaw());
                finalTarget.setPitch(player.getLocation().getPitch());
                player.teleport(finalTarget, PlayerTeleportEvent.TeleportCause.COMMAND,
                        TeleportFlag.Relative.VELOCITY_ROTATION);
                return TpResult.SUCCESSFUL;
            });
        }

        player.sendMessage(getPrefix().append(translatable("ca.teleport.please-wait", NamedTextColor.GRAY)));
        CompletableFuture<TpResult> future = new CompletableFuture<>();

        Location finalTarget = target;
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (future.isDone()) {
                return;
            }

            CaManager.this.teleports.remove(player.getUniqueId());
            player.sendMessage(getPrefix().append(translatable("ca.teleport.teleporting", NamedTextColor.GRAY)));

            finalTarget.getWorld().getChunkAtAsync(finalTarget, true).thenAccept(chunk -> {
                finalTarget.setYaw(player.getLocation().getYaw());
                finalTarget.setPitch(player.getLocation().getPitch());
                player.teleport(finalTarget, PlayerTeleportEvent.TeleportCause.COMMAND,
                        TeleportFlag.Relative.VELOCITY_ROTATION);
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
            player.sendMessage(CaManager.getPrefix().append(translatable("ca.teleport.moved", NamedTextColor.RED)));
        }
        future.complete(result);
    }

    public boolean hasTeleport(Player player) {
        return this.teleports.containsKey(player.getUniqueId());
    }

    public void updateConfig(Consumer<CaConfig> updater) {
        updater.accept(this.getConfig());
        this.saveConfig();
    }

    public void reloadConfig() {
        this.config = CONFIG_LOADER.loadObject(this.configPath, CaConfig.class, CaConfig::new);
        this.leaderboardTasks.handleReload();
    }

    public void saveConfig() {
        CONFIG_LOADER.saveObject(this.configPath, this.getConfig(), CaConfig.class);
    }

    public boolean inElytraBox(Location location) {
        BlockBBox box = getConfig().getSpawnConfig().getElytraBox();
        if (box == null) {
            return false;
        }
        return box.contains(location.getBlock());
    }

    public boolean giveElytra(HumanEntity entity) {
        if (this.hasElytra(entity)) {
            return false;
        }

        ItemStack item = entity.getEquipment().getChestplate();
        byte[] itemBytes = item == null || item.isEmpty() ? new byte[0] : item.serializeAsBytes();
        entity.getPersistentDataContainer().set(this.elytraDataKey, PersistentDataType.BYTE_ARRAY, itemBytes);

        OptionalInt boosts = OptionalInt.of(this.config.getSpawnConfig().getElytraBoosts());
        this.setRemainingElytraBoosts(entity, boosts);
        entity.getEquipment().setChestplate(buildElytraStack(), false);
        return true;
    }

    public boolean removeElytra(HumanEntity entity) {
        if (!this.hasElytra(entity)) {
            return false;
        }
        this.setRemainingElytraBoosts(entity, OptionalInt.empty());

        byte[] itemBytes = entity.getPersistentDataContainer().get(this.elytraDataKey, PersistentDataType.BYTE_ARRAY);
        Objects.requireNonNull(itemBytes, () -> "Invalid elytra data set for key " + this.elytraDataKey);

        ItemStack item = itemBytes.length == 0 ? ItemStack.empty() : ItemStack.deserializeBytes(itemBytes);
        entity.getPersistentDataContainer().remove(this.elytraDataKey);
        entity.getEquipment().setChestplate(item, false);
        return true;
    }

    public boolean hasElytra(PersistentDataHolder holder) {
        return holder.getPersistentDataContainer().has(this.elytraDataKey, PersistentDataType.BYTE_ARRAY);
    }

    public OptionalInt getRemainingElytraBoosts(PersistentDataHolder holder) {
        Integer boosts = holder.getPersistentDataContainer().get(this.elytraBoostsKey, PersistentDataType.INTEGER);
        return boosts == null ? OptionalInt.empty() : OptionalInt.of(boosts);
    }

    public void setRemainingElytraBoosts(PersistentDataHolder holder, OptionalInt boosts) {
        if (boosts.isEmpty()) {
            holder.getPersistentDataContainer().remove(this.elytraBoostsKey);
        } else {
            holder.getPersistentDataContainer().set(this.elytraBoostsKey,
                    PersistentDataType.INTEGER, boosts.getAsInt());
        }
    }

    public boolean canBoostElytra(PersistentDataHolder holder) {
        return this.getRemainingElytraBoosts(holder).orElse(0) > 0;
    }

    public OptionalInt consumeElytraBoost(PersistentDataHolder holder) {
        int boosts = this.getRemainingElytraBoosts(holder).orElse(0);
        if (boosts > 0) {
            OptionalInt newBoosts = OptionalInt.of(boosts - 1);
            this.setRemainingElytraBoosts(holder, newBoosts);
            return newBoosts;
        }
        return OptionalInt.empty();
    }

    public MenuManager getMenus() {
        return this.menus;
    }

    public LeaderboardTasks getLeaderboardTasks() {
        return this.leaderboardTasks;
    }

    public CaConfig getConfig() {
        return Objects.requireNonNull(this.config, "Config has not been loaded yet");
    }

    public Plugin getPlugin() {
        return this.plugin;
    }
}
