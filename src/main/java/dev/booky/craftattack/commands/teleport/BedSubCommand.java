package dev.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:45 01.03.21)

import dev.booky.craftattack.utils.CraftAttackManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class BedSubCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CraftAttackManager manager;

    public BedSubCommand(CraftAttackManager manager) {
        super("bed");
        this.manager = manager;

        withPermission("craftattack.command.teleport.bed");
        executesPlayer(this);
    }

    @Override
    public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
        if (manager.teleportRunnables().containsKey(sender.getUniqueId())) {
            manager.fail(sender, "You are already teleporting");
            return;
        }

        Location location = sender.getBedSpawnLocation();
        if (location == null) {
            manager.fail(sender, "Warning, you will be teleported to world spawn");
            location = manager.config().spawnLocation();

            if (location == null) {
                location = sender.getWorld().getSpawnLocation();
            }
        }

        if (sender.getAllowFlight()) {
            manager.message(sender, "You have been brought to your bed location");
            sender.teleportAsync(location, TeleportCause.COMMAND);
            return;
        }

        Location finalLocation = location;
        manager.message(sender, "Please don't move, you will get teleported in five seconds");
        manager.teleportRunnables().put(sender.getUniqueId(), Bukkit.getScheduler().runTaskLater(manager.plugin(), () -> {
            manager.teleportRunnables().remove(sender.getUniqueId());
            sender.teleportAsync(finalLocation, TeleportCause.COMMAND);
            manager.message(sender, "You have been brought to your bed location");
        }, 5 * 20));
    }
}