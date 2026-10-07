package dev.booky.craftattack.commands.tag;
// Created by booky10 in CraftAttack (07.10.26)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class TagClearCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CaManager manager;

    public TagClearCommand(CaManager manager) {
        super("clear");
        this.manager = manager;

        this.withPermission("craftattack.command.tag");
        this.executesPlayer(this);
    }

    @Override
    public void run(Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        this.manager.getTagManager().clearTag(sender);
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                "ca.command.tag.clear.success", NamedTextColor.GREEN)));
    }
}
