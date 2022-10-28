package dev.booky.craftattack.commands.admin.end;
// Created by booky10 in Kingdoms (12:29 09.04.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class EndActivationSetCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public EndActivationSetCommand(CaManager manager) {
        super("activate");
        this.manager = manager;

        super.withArguments(new BooleanArgument("active"));
        super.withPermission("craftattack.command.admin.end.activate.set");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        boolean activate = (boolean) args[0];

        if (this.manager.getConfig().getEndConfig().isActivated() == activate) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.end.activate.set.already", NamedTextColor.RED)
                    .args(Component.translatable("ca.command.admin.end." + (activate ? "activated" : "deactivated")))));
            return;
        }

        this.manager.updateConfig(config -> config.getEndConfig().setActivated(activate));
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.end.activate.set.success", NamedTextColor.GREEN)
                .args(Component.translatable("ca.command.admin.end." + (activate ? "activated" : "deactivated")))));
    }
}
