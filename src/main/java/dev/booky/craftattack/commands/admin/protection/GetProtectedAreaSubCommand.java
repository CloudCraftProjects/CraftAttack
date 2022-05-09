package dev.booky.craftattack.commands.admin.protection;
// Created by booky10 in CraftAttack (18:53 04.11.21)

import dev.booky.craftattack.utils.CraftAttackManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.BoundingBox;

public class GetProtectedAreaSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CraftAttackManager manager;

    public GetProtectedAreaSubCommand(CraftAttackManager manager) {
        super("protection");
        this.manager = manager;

        withArguments(new LiteralArgument("get"));
        withPermission("craftattack.command.admin.protection.get").executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        BoundingBox boundingBox = manager.config().protectedArea();

        if (boundingBox.getMin().equals(boundingBox.getMax())) {
            manager.fail("No protected area is set!");
        } else {
            manager.message(sender, "The current protected area reaches from " +
                boundingBox.getMinX() + " " + boundingBox.getMinY() + " " + boundingBox.getMinZ() + " to " +
                boundingBox.getMaxX() + " " + boundingBox.getMaxY() + " " + boundingBox.getMaxZ() + ".");
        }
    }
}
