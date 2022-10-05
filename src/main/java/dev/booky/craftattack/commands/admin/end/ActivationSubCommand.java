package dev.booky.craftattack.commands.admin.end;
// Created by booky10 in Kingdoms (12:29 09.04.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ActivationSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public ActivationSubCommand(CaManager manager) {
        super("activate");
        this.manager = manager;

        withArguments(new BooleanArgument("active"));
        withPermission("craftattack.command.admin.end.activate");
        executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        boolean activate = (boolean) args[0];

        if (manager.getConfig().getEndConfig().isActivated() == activate) {
            manager.fail(sender, "The end is already " + (activate ? "" : "de") + "activated");
            return;
        }

        manager.updateConfig(config -> config.getEndConfig().setActivated(activate));
        manager.message(sender, "The end has been " + (activate ? "" : "de") + "activated");
    }
}
