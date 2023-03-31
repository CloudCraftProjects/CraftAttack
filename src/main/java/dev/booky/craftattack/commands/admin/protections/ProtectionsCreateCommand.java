package dev.booky.craftattack.commands.admin.protections;
// Created by booky10 in CraftAttack (22:42 05.10.22)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaBoundingBox;
import dev.booky.craftattack.utils.ProtectedArea;
import dev.jorel.commandapi.CommandAPICommand;
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

import java.util.Objects;

public class ProtectionsCreateCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public ProtectionsCreateCommand(CaManager manager) {
        super("create");
        this.manager = manager;

        super.withArguments(new WorldArgument("dimension"));
        super.withArguments(new LocationArgument("corner1", LocationType.BLOCK_POSITION));
        super.withArguments(new LocationArgument("corner2", LocationType.BLOCK_POSITION));

        super.withPermission("craftattack.command.admin.protections.create");
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

        if (this.manager.getConfig().getProtectedAreas().contains(area)) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.protections.create.already", NamedTextColor.RED)));
            return;
        }

        // We can simply add this box to the areas, because configurate deserializes Set's
        // as a **modifiable** LinkedHashSet, see SetSerializer line 55.
        this.manager.updateConfig(config -> config.getProtectedAreas().add(area));
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.protections.create.success", NamedTextColor.GREEN)));
    }
}
