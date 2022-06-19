package dev.booky.craftattack.commands.admin.spawn;
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

        withPermission("craftattack.command.admin.spawn.radius.get");
        withArguments(new LiteralArgument("get"));
        executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        int radius = manager.config().spawnRadius();

        if (radius <= -1) {
            manager.fail(sender, "The spawn radius has not been set yet");
            return;
        }

        manager.message(sender, "The spawn radius is currently at " + radius + " blocks");
    }
}
