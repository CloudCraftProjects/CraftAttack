package dev.booky.craftattack.commands.admin.dimension;
// Created by booky10 in Kingdoms (12:29 09.04.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class DimensionActivationGetCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;
    private final String id;
    private final Key dimension;

    public DimensionActivationGetCommand(CaManager manager, String id, Key dimension) {
        super("activate");
        this.manager = manager;
        this.id = id;
        this.dimension = dimension;

        super.withPermission("craftattack.command.admin." + id + ".activate.get");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        String activatedStr = this.manager.getConfig().getDimensionConfig(this.dimension).isActivated() ? "activated" : "deactivated";
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin." + this.id + ".activate.get.message", NamedTextColor.GREEN)
                .arguments(Component.translatable("ca.command.admin." + this.id + "." + activatedStr))));
    }
}
