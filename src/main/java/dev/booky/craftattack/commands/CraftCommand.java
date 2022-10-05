package dev.booky.craftattack.commands;
// Created by booky10 in CraftAttack (21:08 17.07.22)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CraftCommand extends CommandAPICommand implements PlayerCommandExecutor {

    public CraftCommand() {
        super("craft");
        super.withPermission("craftattack.command.craft");
        super.executesPlayer(this);

        // Additionally register just /craft as a command
        super.register();
    }

    @Override
    public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
        if (!sender.getInventory().contains(Material.CRAFTING_TABLE)) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.craft.no-table-found", NamedTextColor.RED)));
            return;
        }

        sender.openWorkbench(null, true);
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.craft.success", NamedTextColor.GREEN)));
    }
}
