package dev.booky.craftattack.commands.admin.protections;
// Created by booky10 in CraftAttack (22:42 05.10.22)

import com.google.common.base.Preconditions;
import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaBoundingBox;
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

public class ProtectionsDeleteCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public ProtectionsDeleteCommand(CaManager manager) {
        super("delete");
        this.manager = manager;

        super.withArguments(new NamespacedKeyArgument("level")
                .includeSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getWorlds().stream()
                        .map(World::getKey).map(NamespacedKey::asString).toArray(String[]::new))));
        super.withArguments(new LocationArgument("corner1", LocationType.BLOCK_POSITION));
        super.withArguments(new LocationArgument("corner2", LocationType.BLOCK_POSITION));

        super.withPermission("craftattack.command.admin.protections.delete");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        World world = Preconditions.checkNotNull(Bukkit.getWorld((NamespacedKey) args[0]), "Unknown world " + args[0]);
        Location corner1 = (Location) args[1], corner2 = (Location) args[2];
        corner1.setWorld(world);
        corner2.setWorld(world);

        CaBoundingBox box = new CaBoundingBox(corner1, corner2);
        if (!this.manager.getConfig().getProtectedAreas().contains(box)) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.protections.delete.not-found", NamedTextColor.RED)));
            return;
        }

        // For why can do this, see ProtectionsCreateCommand line 53+54.
        this.manager.updateConfig(config -> config.getProtectedAreas().remove(box));
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.protections.delete.success", NamedTextColor.GREEN)));
    }
}
