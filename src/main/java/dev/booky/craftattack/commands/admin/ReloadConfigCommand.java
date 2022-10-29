package dev.booky.craftattack.commands.admin;
// Created by booky10 in CraftAttack (13:16 29.10.22)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class ReloadConfigCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public ReloadConfigCommand(CaManager manager) {
        super("reload");
        this.manager = manager;

        super.withPermission("craftattack.command.admin.reload-config");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        this.manager.reloadConfig();
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.reload-config", NamedTextColor.GREEN)));
    }
}
