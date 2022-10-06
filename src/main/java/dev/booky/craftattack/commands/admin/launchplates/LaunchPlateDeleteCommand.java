package dev.booky.craftattack.commands.admin.launchplates;
// Created by booky10 in CraftAttack (22:42 05.10.22)

import com.google.common.base.Preconditions;
import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
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
import org.bukkit.entity.Player;

public class LaunchPlateDeleteCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public LaunchPlateDeleteCommand(CaManager manager, boolean withPosition) {
        super("delete");
        this.manager = manager;

        if (withPosition) {
            super.withArguments(new NamespacedKeyArgument("level")
                    .includeSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getWorlds().stream()
                            .map(World::getKey).map(NamespacedKey::asString).toArray(String[]::new))));
            super.withArguments(new LocationArgument("block", LocationType.BLOCK_POSITION));

            super.executes(this);
        } else {
            super.executesPlayer(this::run);
        }

        super.withPermission("craftattack.command.admin.launches.delete");
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        Location block;
        if (args.length == 2) {
            World world = Preconditions.checkNotNull(Bukkit.getWorld((NamespacedKey) args[0]), "Unknown world " + args[0]);
            block = (Location) args[1];
            block.setWorld(world);
        } else if (sender instanceof Player) {
            block = ((Player) sender).getLocation().toBlockLocation();
            block.setYaw(0f);
            block.setPitch(0f);
        } else {
            throw new AssertionError();
        }

        if (!this.manager.getConfig().getLaunchPlates().contains(block)) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.launches.delete.not-found", NamedTextColor.RED)));
            return;
        }

        this.manager.updateConfig(config -> config.getLaunchPlates().remove(block));
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.launches.delete.success", NamedTextColor.GREEN)));
    }
}
