package dev.booky.craftattack.commands.admin.protections.flags;
// Created by booky10 in CraftAttack (19:55 14.11.22)

import com.google.common.base.Preconditions;
import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaBoundingBox;
import dev.booky.craftattack.utils.ProtectedArea;
import dev.booky.craftattack.utils.ProtectionFlag;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.ListArgumentBuilder;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FlagAddCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public FlagAddCommand(CaManager manager) {
        super("add");
        this.manager = manager;

        super.withArguments(new NamespacedKeyArgument("level")
                .includeSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getWorlds().stream()
                        .map(World::getKey).map(NamespacedKey::asString).toArray(String[]::new))));
        super.withArguments(new LocationArgument("corner1", LocationType.BLOCK_POSITION));
        super.withArguments(new LocationArgument("corner2", LocationType.BLOCK_POSITION));
        super.withArguments(new ListArgumentBuilder<String>("flags")
                .withList(Arrays.stream(ProtectionFlag.values()).map(ProtectionFlag::name).toList())
                .withStringMapper().build());

        super.withPermission("craftattack.command.admin.protections.flag.add");
        super.executes(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        World world = Preconditions.checkNotNull(Bukkit.getWorld((NamespacedKey) args[0]), "Unknown world " + args[0]);
        Location corner1 = (Location) args[1], corner2 = (Location) args[2];
        corner1.setWorld(world);
        corner2.setWorld(world);

        CaBoundingBox box = new CaBoundingBox(corner1, corner2);
        box.expand(0d, 0d, 0d, 1d, 1d, 1d);
        ProtectedArea area = new ProtectedArea(box);

        if (!this.manager.getConfig().getProtectedAreas().contains(area)) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.protections.flag.not-found", NamedTextColor.RED)));
            return;
        } else {
            area = this.manager.getConfig().getProtectedAreas().stream().filter(area::equals).findAny().orElseThrow();
        }

        List<String> rawFlags = (List<String>) args[3];
        Set<ProtectionFlag> flags = rawFlags.stream()
                .map(String::toUpperCase)
                .map(ProtectionFlag::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        if (flags.isEmpty()) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.protections.flag.add.no-flags", NamedTextColor.RED)));
            return;
        }

        ProtectedArea finalArea = area;
        this.manager.updateConfig(config -> {
            for (ProtectionFlag flag : flags) {
                if (!finalArea.addFlag(flag)) {
                    sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.protections.flag.add.failure", NamedTextColor.RED)
                            .args(flag.getTranslation())));
                    continue;
                }

                sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.protections.flag.add.success", NamedTextColor.GREEN)
                        .args(flag.getTranslation())));
            }
        });
    }
}
