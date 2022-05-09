package dev.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:54 03.01.21)

import dev.booky.craftattack.utils.CraftAttackManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static org.bukkit.Bukkit.getScheduler;
import static org.bukkit.GameMode.CREATIVE;
import static org.bukkit.GameMode.SPECTATOR;

public class EndSubCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final Set<UUID> currentlyTeleporting = new HashSet<>();
    private final CraftAttackManager manager;

    public EndSubCommand(CraftAttackManager manager) {
        super("end");
        this.manager = manager;

        withPermission("craftattack.command.teleport.end").executesPlayer(this);
    }

    @Override
    public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
        if (manager.config().endLocation() == null) {
            manager.fail("The end location has not been set yet!");
        } else {
            if (currentlyTeleporting.add(sender.getUniqueId())) {
                manager.message(sender, "Please don't move, you will get teleported in five seconds.");
                Location oldLocation = sender.getLocation().getBlock().getLocation();

                getScheduler().runTaskLater(manager.plugin(), () -> {
                    currentlyTeleporting.remove(sender.getUniqueId());

                    if (sender.isOnline()) {
                        if (oldLocation.equals(sender.getLocation().getBlock().getLocation())) {
                            sender.teleportAsync(manager.config().endLocation(), TeleportCause.COMMAND);
                            manager.message(sender, "You have been brought to the end location!");
                        } else {
                            manager.message(sender, text("You have moved.", RED));
                        }
                    }
                }, sender.getAllowFlight() ? 0 : 5 * 20);
            } else {
                manager.fail("You are already teleporting.");
            }
        }
    }
}
