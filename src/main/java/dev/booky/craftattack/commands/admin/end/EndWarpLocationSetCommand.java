package dev.booky.craftattack.commands.admin.end;
// Created by booky10 in CraftAttack (15:52 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.RotationArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.wrappers.Rotation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class EndWarpLocationSetCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public EndWarpLocationSetCommand(CaManager manager, boolean withAngles) {
        super("location");
        this.manager = manager;

        super.withArguments(new LocationArgument("location", LocationType.PRECISE_POSITION));
        if (withAngles) {
            super.withArguments(new RotationArgument("rotation"));
        }

        super.withPermission("craftattack.command.admin.end.location.set");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        Location location = (Location) args[0];
        if (args.length > 1) {
            Rotation rotation = (Rotation) args[1];
            location.setYaw(rotation.getYaw());
            location.setPitch(rotation.getPitch());
        }

        if (location.equals(this.manager.getConfig().getEndConfig().getWarpLocation())) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.end.location.set.already-set", NamedTextColor.RED)));
            return;
        }

        this.manager.updateConfig(config -> config.getEndConfig().setWarpLocation(location));
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.end.location.set.success", NamedTextColor.GREEN)));
    }
}
