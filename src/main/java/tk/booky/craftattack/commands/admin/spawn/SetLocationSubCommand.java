package tk.booky.craftattack.commands.admin.spawn;
// Created by booky10 in CraftAttack (15:52 01.03.21)

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.AngleArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import tk.booky.craftattack.utils.CraftAttackManager;

public class SetLocationSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CraftAttackManager manager;

    public SetLocationSubCommand(CraftAttackManager manager) {
        super("location");
        this.manager = manager;

        withPermission("craftattack.command.admin.spawn.location.set");
        withArguments(new LiteralArgument("set"), new LocationArgument("location", LocationType.PRECISE_POSITION), new AngleArgument("yaw"));
        executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        Location location = (Location) args[0];
        location.setYaw((float) args[1]);

        if (location.equals(manager.config().spawnLocation())) {
            manager.fail("The spawn location is already at the exact same position!");
        } else {
            manager.config().spawnLocation(location);
            manager.message(sender, "The spawn location has been set!");
        }
    }
}
