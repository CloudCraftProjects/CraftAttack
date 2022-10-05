package dev.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:54 03.01.21)

import dev.booky.craftattack.utils.CraftAttackManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class EndSubCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CraftAttackManager manager;

    public EndSubCommand(CraftAttackManager manager) {
        super("end");
        this.manager = manager;

        withPermission("craftattack.command.teleport.end");
        executesPlayer(this);
    }

    @Override
    public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
        if (manager.getConfig().getEndConfig().getWarpLocation() == null) {
            manager.fail(sender, "The end location has not been set yet");
            return;
        }

        if (manager.teleportRunnables().containsKey(sender.getUniqueId())) {
            manager.fail(sender, "You are already teleporting");
            return;
        }

        if (sender.getAllowFlight()) {
            manager.message(sender, "You have been brought to the end location");
            sender.teleportAsync(manager.getConfig().getEndConfig().getWarpLocation(), TeleportCause.COMMAND);
            return;
        }

        manager.message(sender, "Please don't move, you will get teleported in five seconds");
        manager.teleportRunnables().put(sender.getUniqueId(), Bukkit.getScheduler().runTaskLater(manager.getMain(), () -> {
            manager.teleportRunnables().remove(sender.getUniqueId());
            sender.teleportAsync(manager.getConfig().getEndConfig().getWarpLocation(), TeleportCause.COMMAND);
            manager.message(sender, "You have been brought to the end location");
        }, 5 * 20));
    }
}
