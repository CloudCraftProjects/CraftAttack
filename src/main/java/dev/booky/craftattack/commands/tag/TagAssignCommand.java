package dev.booky.craftattack.commands.tag;
// Created by booky10 in CraftAttack (07.10.26)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

public class TagAssignCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CaManager manager;

    public TagAssignCommand(CaManager manager) {
        super("assign");
        this.manager = manager;

        this.withPermission("craftattack.command.tag");
        this.withArguments(this.manager.getTagManager().tagArgument("tag"));
        this.executesPlayer(this);
    }

    @Override
    public void run(Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        String input = Objects.requireNonNull(args.getUnchecked("tag"));
        Map<String, CaConfig.TagEntry> tags = this.manager.getConfig().getTags();
        String key = this.manager.getTagManager().resolveTagKey(input);
        if (key == null) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.tag.invalid", NamedTextColor.RED, Component.text(input))));
            return;
        }

        this.manager.getTagManager().assignTag(sender, key);
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                "ca.command.tag.assign.success", NamedTextColor.GREEN,
                this.manager.getTagManager().renderDisplay(tags.get(key).getDisplay()))));
    }
}
