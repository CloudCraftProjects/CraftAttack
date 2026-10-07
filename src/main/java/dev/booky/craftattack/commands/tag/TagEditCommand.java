package dev.booky.craftattack.commands.tag;
// Created by booky10 in CraftAttack (07.10.26)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig;
import dev.booky.craftattack.utils.TagManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

public class TagEditCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CaManager manager;

    public TagEditCommand(CaManager manager) {
        super("edit");
        this.manager = manager;

        this.withPermission("craftattack.command.tag");
        this.withArguments(
                this.manager.getTagManager().tagArgument("tag"),
                new GreedyStringArgument("display"));
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

        CaConfig.TagEntry tag = tags.get(key);
        if (!this.manager.getTagManager().canManage(sender, tag)) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.tag.no-permission", NamedTextColor.RED)));
            return;
        }

        String displayInput = Objects.requireNonNull(args.getUnchecked("display"));
        String display = this.manager.getTagManager().validateDisplay(displayInput);
        if (display == null) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.tag.invalid-display", NamedTextColor.RED,
                    Component.text(TagManager.MAX_DISPLAY_LENGTH))));
            return;
        }

        String oldDisplay = tag.getDisplay();
        this.manager.updateConfig(config -> config.getTags().get(key).setDisplay(display));
        this.manager.getTagManager().getAssignees(oldDisplay).thenAccept(assignees -> {
            for (var uuid : assignees) {
                this.manager.getTagManager().setSuffix(uuid, display);
            }
        });

        sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                "ca.command.tag.edit.success", NamedTextColor.GREEN,
                Component.text(key), this.manager.getTagManager().renderDisplay(display))));
    }
}
