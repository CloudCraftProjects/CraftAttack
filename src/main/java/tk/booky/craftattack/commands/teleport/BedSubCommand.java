package tk.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:45 01.03.21)

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
import static org.bukkit.GameMode.CREATIVE;
import static org.bukkit.GameMode.SPECTATOR;

public class BedSubCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final Set<UUID> currentlyTeleporting = new HashSet<>();
    private final CraftAttackManager manager;

    public BedSubCommand(CraftAttackManager manager) {
        super("bed");
        this.manager = manager;

        withPermission("craftattack.command.teleport.bed").executesPlayer(this);
    }

    @Override
    public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
        if (currentlyTeleporting.add(sender.getUniqueId())) {
            manager.message(sender, "Please don't move, you will get teleported in five seconds.");
            Location oldLocation = sender.getLocation().toBlockLocation();

            getScheduler().runTaskLater(manager.plugin(), () -> {
                currentlyTeleporting.remove(sender.getUniqueId());

                if (sender.isOnline()) {
                    if (oldLocation.equals(sender.getLocation().toBlockLocation())) {
                        Location location = sender.getBedSpawnLocation();

                        if (location == null) {
                            sender.teleportAsync(manager.overworld().getSpawnLocation().toCenterLocation(), TeleportCause.COMMAND);
                            manager.message(sender, "Your bed was broken and you have been send to the world spawn!");
                        } else {
                            sender.teleportAsync(location.toCenterLocation(), TeleportCause.COMMAND);
                            manager.message(sender, "You have been brought back to your bed!");
                        }
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
