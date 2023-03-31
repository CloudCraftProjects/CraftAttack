package dev.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:54 03.01.21)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.TpResult;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class TeleportEndCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CaManager manager;

    public TeleportEndCommand(CaManager manager) {
        super("end");
        this.manager = manager;

        super.withPermission("craftattack.command.teleport.end");
        super.executesPlayer(this);

        // Additionally register just /end as a command
        super.register();
    }

    @Override
    public void run(Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        this.manager.teleportRequest(sender, this.manager.getConfig().getEndConfig().getWarpLocation()).thenAccept(result -> {
            if (result == TpResult.SUCCESSFUL) {
                sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.teleport.end", NamedTextColor.GREEN)));
            }
        });
    }
}
