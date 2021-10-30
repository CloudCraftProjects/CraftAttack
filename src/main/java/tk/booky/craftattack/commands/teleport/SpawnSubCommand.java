package tk.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:55 01.03.21)

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import tk.booky.craftattack.utils.CraftAttackManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static org.bukkit.Bukkit.getScheduler;

public class SpawnSubCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final Set<UUID> currentlyTeleporting = new HashSet<>();
    private final CraftAttackManager manager;

    public SpawnSubCommand(CraftAttackManager manager) {
        super("spawn");
        this.manager = manager;

        withPermission("craftattack.command.teleport.spawn").executesPlayer(this);
    }

    @Override
    public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
        if (manager.config().spawnLocation() == null) {
            manager.fail("The spawn location has not been set yet!");
        } else {
            if (currentlyTeleporting.add(sender.getUniqueId())) {
                manager.message(sender, "Please don't move, you will get teleported in five seconds.");
                Location oldLocation = sender.getLocation().toBlockLocation();

                getScheduler().runTaskLater(manager.plugin(), () -> {
                    currentlyTeleporting.remove(sender.getUniqueId());

                    if (sender.isOnline()) {
                        if (oldLocation.equals(sender.getLocation().toBlockLocation())) {
                            sender.teleportAsync(manager.config().spawnLocation().toCenterLocation(), TeleportCause.COMMAND);
                            manager.message(sender, "You have been brought to the spawn location!");
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
