package dev.booky.craftattack.commands.admin.end;
// Created by booky10 in CraftAttack (15:51 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class EndWarpLocationGetCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public EndWarpLocationGetCommand(CaManager manager) {
        super("warp-location");
        this.manager = manager;

        super.withPermission("craftattack.command.admin.end.location.get");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        Location location = this.manager.getConfig().getEndConfig().getWarpLocation();
        if (location == null) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.end.location.get.not-set", NamedTextColor.RED)));
            return;
        }

        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.end.location.get.success", NamedTextColor.GREEN).args(
                Component.text(location.getX()), Component.text(location.getY()), Component.text(location.getZ()),
                Component.text(location.getYaw()), Component.text(location.getPitch()),
                Component.text(location.getWorld().getKey().asString()))));
    }
}
