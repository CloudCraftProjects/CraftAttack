package dev.booky.craftattack.commands.admin.dimension;
// Created by booky10 in CraftAttack (15:51 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class DimensionWarpLocationGetCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;
    private final String id;
    private final Key dimension;

    public DimensionWarpLocationGetCommand(CaManager manager, String id, Key dimension) {
        super("location");
        this.manager = manager;
        this.id = id;
        this.dimension = dimension;

        super.withPermission("craftattack.command.admin." + id + ".location.get");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        Location location = this.manager.getConfig().getDimensionConfig(this.dimension).getWarpLocation();
        if (location == null) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin." + this.id + ".location.get.not-set", NamedTextColor.RED)));
            return;
        }

        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin." + this.id + ".location.get.success", NamedTextColor.GREEN).arguments(
                Component.text(location.getX()), Component.text(location.getY()), Component.text(location.getZ()),
                Component.text(location.getYaw()), Component.text(location.getPitch()),
                Component.text(location.getWorld().getKey().asString()))));
    }
}
