package dev.booky.craftattack.commands;
// Created by booky10 in CraftAttack (21:08 17.07.22)

import dev.booky.craftattack.utils.CraftAttackManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CraftCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CraftAttackManager manager;

    public CraftCommand(CraftAttackManager manager) {
        super("craft");
        this.manager = manager;

        withPermission("craftattack.command.craft");
        executesPlayer(this);
    }

    @Override
    public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
        if (!sender.getInventory().contains(Material.CRAFTING_TABLE)) {
            manager.fail(sender, "You have to have at least one crafting table in your inventory");
            return;
        }

        sender.openWorkbench(null, true);
        manager.message(sender, "Opened a crafting table for you");
    }
}
