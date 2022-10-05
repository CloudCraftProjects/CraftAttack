package dev.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:55 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.TpResult;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class TeleportSpawnCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CaManager manager;

    public TeleportSpawnCommand(CaManager manager) {
        super("spawn");
        this.manager = manager;

        super.withPermission("craftattack.command.teleport.spawn");
        super.executesPlayer(this);

        // Additionally register just /spawn as a command
        super.register();
    }

    @Override
    public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
        this.manager.teleportRequest(sender, this.manager.getConfig().getSpawnConfig().getWarpLocation()).thenAccept(result -> {
            if (result == TpResult.SUCCESSFUL) {
                sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.teleport.spawn", NamedTextColor.GREEN)));
            }
        });
    }
}
