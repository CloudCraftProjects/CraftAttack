package tk.booky.craftattack.commands.admin;
// Created by booky10 in CraftAttack (15:45 01.03.21)

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import tk.booky.craftattack.manager.CraftAttackManager;

public class SetEndLocationSubCommand extends CommandAPICommand implements CommandExecutor {

    public SetEndLocationSubCommand() {
        super("setEndLocation");

        withPermission("craftattack.command.admin.end.set");
        withArguments(new LocationArgument("location", LocationType.PRECISE_POSITION), new FloatArgument("yaw"), new FloatArgument("pitch"));

        executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        Location location = (Location) args[0];

        location.setYaw((float) args[1]);
        location.setPitch((float) args[2]);

        if (CraftAttackManager.getEndLocation() == location) {
            CommandAPI.fail("The end location is already at the exact same position!");
        } else {
            CraftAttackManager.setEndLocation(location);
            CraftAttackManager.save(true);

            sender.sendMessage("The end location has been set!");
        }
    }
}
