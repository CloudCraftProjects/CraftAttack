package tk.booky.craftattack.commands.admin.protection;
// Created by booky10 in CraftAttack (18:53 04.11.21)

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.util.BoundingBox;
import tk.booky.craftattack.utils.CraftAttackManager;

public class SetProtectedAreaSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CraftAttackManager manager;

    public SetProtectedAreaSubCommand(CraftAttackManager manager) {
        super("protection");
        this.manager = manager;

        withArguments(
            new LiteralArgument("set"),
            new LocationArgument("location1", LocationType.BLOCK_POSITION),
            new LocationArgument("location2", LocationType.BLOCK_POSITION)
        );
        withPermission("craftattack.command.admin.protection.set").executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        Location location1 = (Location) args[0], location2 = (Location) args[1];
        BoundingBox newBoundingBox = BoundingBox.of(location1, location2);

        if (newBoundingBox.equals(manager.config().protectedArea())) {
            manager.fail("The protected area is already at the exact same area!");
        } else {
            manager.config().protectedArea(newBoundingBox);
            manager.message(sender, "The protected area has been set!");
        }
    }
}
