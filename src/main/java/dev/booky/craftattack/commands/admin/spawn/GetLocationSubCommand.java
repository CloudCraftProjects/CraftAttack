package dev.booky.craftattack.commands.admin.spawn;
// Created by booky10 in CraftAttack (15:51 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class GetLocationSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public GetLocationSubCommand(CaManager manager) {
        super("location");
        this.manager = manager;

        withPermission("craftattack.command.admin.spawn.location.get");
        withArguments(new LiteralArgument("get"));
        executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        Location location = manager.getConfig().getSpawnConfig().getWarpLocation();

        if (location == null) {
            manager.fail(sender, "The spawn location has not been set yet");
            return;
        }

        manager.message(sender, String.format(
                "The spawn location is currently at %s %s %s %s %s in %s",
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch(),
                location.getWorld().getName()
        ));
    }
}
