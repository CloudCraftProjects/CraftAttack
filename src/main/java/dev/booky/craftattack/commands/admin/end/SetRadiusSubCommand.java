package dev.booky.craftattack.commands.admin.end;
// Created by booky10 in CraftAttack (15:04 01.03.21)

import dev.booky.craftattack.utils.CraftAttackManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetRadiusSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CraftAttackManager manager;

    public SetRadiusSubCommand(CraftAttackManager manager) {
        super("radius");
        this.manager = manager;

        withArguments(
                new LiteralArgument("set"),
                new IntegerArgument("radius", 0));
        withPermission("craftattack.command.admin.end.radius.set");
        executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        int radius = (int) args[0];

        if (manager.config().endRadius() == radius) {
            manager.fail(sender, "The end radius is already at this size");
            return;
        }

        manager.config().endRadius(radius);
        manager.message(sender, "The end radius has been set to " + radius);
    }
}
