package dev.booky.craftattack.commands.admin.end;
// Created by booky10 in Kingdoms (12:29 09.04.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class EndActivationGetCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public EndActivationGetCommand(CaManager manager) {
        super("activate");
        this.manager = manager;

        super.withPermission("craftattack.command.admin.end.activate.get");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        String activatedStr = this.manager.getConfig().getEndConfig().isActivated() ? "activated" : "deactivated";
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.end.activate.get.message", NamedTextColor.GREEN)
                .args(Component.translatable("ca.command.admin.end." + activatedStr, NamedTextColor.GREEN))));
    }
}
