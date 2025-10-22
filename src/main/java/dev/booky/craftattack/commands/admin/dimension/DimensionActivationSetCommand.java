package dev.booky.craftattack.commands.admin.dimension;
// Created by booky10 in Kingdoms (12:29 09.04.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public class DimensionActivationSetCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;
    private final String id;
    private final Key dimension;

    public DimensionActivationSetCommand(CaManager manager, String id, Key dimension) {
        super("activate");
        this.manager = manager;
        this.id = id;
        this.dimension = dimension;

        super.withArguments(new BooleanArgument("active"));
        super.withPermission("craftattack.command.admin." + id + ".activate.set");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        boolean activate = Objects.requireNonNull(args.<Boolean>getUnchecked("active"));

        if (this.manager.getConfig().getDimensionConfig(this.dimension).isActivated() == activate) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin." + this.id + ".activate.set.already", NamedTextColor.RED)
                    .arguments(Component.translatable("ca.command.admin." + this.id + "." + (activate ? "activated" : "deactivated")))));
            return;
        }

        this.manager.updateConfig(config -> config.getDimensionConfig(this.dimension).setActivated(activate));
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin." + this.id + ".activate.set.success", NamedTextColor.GREEN)
                .arguments(Component.translatable("ca.command.admin." + this.id + "." + (activate ? "activated" : "deactivated")))));
    }
}
