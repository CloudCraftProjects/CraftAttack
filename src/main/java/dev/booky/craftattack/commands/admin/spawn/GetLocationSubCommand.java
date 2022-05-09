package dev.booky.craftattack.commands.admin.spawn;
// Created by booky10 in CraftAttack (15:51 01.03.21)

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import dev.booky.craftattack.utils.CraftAttackManager;

public class GetLocationSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CraftAttackManager manager;

    public GetLocationSubCommand(CraftAttackManager manager) {
        super("location");
        this.manager = manager;

        withPermission("craftattack.command.admin.spawn.location.get");
        withArguments(new LiteralArgument("get"));
        executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        Location location = manager.config().spawnLocation();

        if (location == null) {
            manager.fail("The spawn location has not been set yet!");
        } else {
            manager.message(sender, String.format(
                "The spawn location is currently at %s %s %s %s %s in %s!",
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch(),
                location.getWorld().getName()
            ));
        }
    }
}
