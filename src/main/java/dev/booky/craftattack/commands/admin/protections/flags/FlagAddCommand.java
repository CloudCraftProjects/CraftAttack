package dev.booky.craftattack.commands.admin.protections.flags;
// Created by booky10 in CraftAttack (19:55 14.11.22)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaBoundingBox;
import dev.booky.craftattack.utils.ProtectedArea;
import dev.booky.craftattack.utils.ProtectionFlag;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ListArgumentBuilder;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.WorldArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FlagAddCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public FlagAddCommand(CaManager manager) {
        super("add");
        this.manager = manager;

        super.withArguments(new WorldArgument("dimension"));
        super.withArguments(new LocationArgument("corner1", LocationType.BLOCK_POSITION));
        super.withArguments(new LocationArgument("corner2", LocationType.BLOCK_POSITION));
        super.withArguments(new ListArgumentBuilder<ProtectionFlag>("flags")
                .withList(Arrays.stream(ProtectionFlag.values()).toList())
                .withMapper(ProtectionFlag::name).buildGreedy());

        super.withPermission("craftattack.command.admin.protections.flag.add");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        World world = Objects.requireNonNull(args.getUnchecked("dimension"));
        Location corner1 = Objects.requireNonNull(args.getUnchecked("corner1"));
        Location corner2 = Objects.requireNonNull(args.getUnchecked("corner2"));
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

        List<ProtectionFlag> flags = Objects.requireNonNull(args.getUnchecked("flags"));
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
