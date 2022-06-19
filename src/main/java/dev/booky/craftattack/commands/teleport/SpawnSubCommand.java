package dev.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:55 01.03.21)

import dev.booky.craftattack.utils.CraftAttackManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class SpawnSubCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CraftAttackManager manager;

    public SpawnSubCommand(CraftAttackManager manager) {
        super("spawn");
        this.manager = manager;

        withPermission("craftattack.command.teleport.spawn");
        executesPlayer(this);
    }

    @Override
    public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
        if (manager.config().spawnLocation() == null) {
            manager.fail(sender, "The spawn location has not been set yet");
            return;
        }

        if (manager.teleportRunnables().containsKey(sender.getUniqueId())) {
            manager.fail(sender, "You are already teleporting");
            return;
        }

        if (sender.getAllowFlight()) {
            manager.message(sender, "You have been brought to the spawn location");
            sender.teleportAsync(manager.config().spawnLocation(), TeleportCause.COMMAND);
            return;
        }

        manager.message(sender, "Please don't move, you will get teleported in five seconds");
        manager.teleportRunnables().put(sender.getUniqueId(), Bukkit.getScheduler().runTaskLater(manager.plugin(), () -> {
            manager.teleportRunnables().remove(sender.getUniqueId());
            sender.teleportAsync(manager.config().spawnLocation(), TeleportCause.COMMAND);
            manager.message(sender, "You have been brought to the spawn location");
        }, 5 * 20));
    }
}
