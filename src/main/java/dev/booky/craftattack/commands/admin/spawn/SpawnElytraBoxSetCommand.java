package dev.booky.craftattack.commands.admin.spawn;
// Created by booky10 in CraftAttack (21:57 05.10.22)

import dev.booky.cloudcore.util.BlockBBox;
import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public class SpawnElytraBoxSetCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public SpawnElytraBoxSetCommand(CaManager manager) {
        super("elytra-box");
        this.manager = manager;

        super.withArguments(new LocationArgument("corner1", LocationType.BLOCK_POSITION));
        super.withArguments(new LocationArgument("corner2", LocationType.BLOCK_POSITION));

        super.withPermission("craftattack.command.admin.spawn.elytra-box.set");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        Location corner1 = Objects.requireNonNull(args.getUnchecked("corner1"));
        Location corner2 = Objects.requireNonNull(args.getUnchecked("corner2"));
        BlockBBox box = new BlockBBox(corner1, corner2);

        if (box.equals(this.manager.getConfig().getSpawnConfig().getElytraBox())) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.spawn.elytra-box.set.already", NamedTextColor.RED)));
            return;
        }

        this.manager.updateConfig(config -> config.getSpawnConfig().setElytraBox(box));
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.spawn.elytra-box.set.success", NamedTextColor.GREEN)));
    }
}
