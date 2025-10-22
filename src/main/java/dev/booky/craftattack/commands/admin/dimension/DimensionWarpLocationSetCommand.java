package dev.booky.craftattack.commands.admin.dimension;
// Created by booky10 in CraftAttack (15:52 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.RotationArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.wrappers.Rotation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public class DimensionWarpLocationSetCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;
    private final String id;
    private final Key dimension;

    public DimensionWarpLocationSetCommand(CaManager manager, String id, Key dimension) {
        super("location");
        this.manager = manager;
        this.id = id;
        this.dimension = dimension;

        super.withArguments(new LocationArgument("location", LocationType.PRECISE_POSITION));
        super.withOptionalArguments(new RotationArgument("rotation"));

        super.withPermission("craftattack.command.admin." + id + ".location.set");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        Location location = Objects.requireNonNull(args.getUnchecked("location"));
        Rotation rotation = args.getUnchecked("rotation");
        if (rotation != null) {
            location.setYaw(rotation.getYaw());
            location.setPitch(rotation.getPitch());
        }

        if (location.equals(this.manager.getConfig().getDimensionConfig(this.dimension).getWarpLocation())) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin." + this.id + ".location.set.already", NamedTextColor.RED)));
            return;
        }

        this.manager.updateConfig(config -> config.getDimensionConfig(this.dimension).setWarpLocation(location));
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin." + this.id + ".location.set.success", NamedTextColor.GREEN)));
    }
}
