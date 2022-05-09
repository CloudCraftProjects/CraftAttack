package dev.booky.craftattack.commands.admin.end;
// Created by booky10 in CraftAttack (15:10 01.03.21)

import dev.booky.craftattack.utils.CraftAttackManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GetRadiusSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CraftAttackManager manager;

    public GetRadiusSubCommand(CraftAttackManager manager) {
        super("radius");
        this.manager = manager;

        withArguments(new LiteralArgument("get"));

        withPermission("craftattack.command.admin.end.radius.get").executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        int radius = manager.config().endRadius();

        if (radius <= -1) {
            manager.fail("The end radius has not been set yet!");
        } else {
            manager.message(sender, "The end radius is currently at " + radius + " blocks!");
        }
    }
}
