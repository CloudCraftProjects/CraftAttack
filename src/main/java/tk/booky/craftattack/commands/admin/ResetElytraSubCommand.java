package tk.booky.craftattack.commands.admin;
// Created by booky10 in CraftAttack (15:08 24.05.21)

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tk.booky.craftattack.manager.CraftAttackManager;

import java.util.Collection;

public class ResetElytraSubCommand extends CommandAPICommand implements CommandExecutor {

    public ResetElytraSubCommand() {
        super("resetElytra");

        withPermission("craftattack.command.admin.elytra.reset");
        withArguments(new EntitySelectorArgument("targets", EntitySelectorArgument.EntitySelector.MANY_PLAYERS));

        executes(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        Collection<Player> players = (Collection<Player>) args[0];

        for (Player player : players) {
            CraftAttackManager.removeElytra(player);

            if (!CraftAttackManager.isInSpawn(player.getLocation(), null)) continue;
            CraftAttackManager.giveElytra(player);
        }

        sender.sendMessage(players.size() + " players have been reset!");
    }
}