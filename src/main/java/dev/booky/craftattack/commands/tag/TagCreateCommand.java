package dev.booky.craftattack.commands.tag;
// Created by booky10 in CraftAttack (07.10.26)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig;
import dev.booky.craftattack.utils.TagManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Objects;

public class TagCreateCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CaManager manager;

    public TagCreateCommand(CaManager manager) {
        super("create");
        this.manager = manager;

        this.withPermission("craftattack.command.tag");
        this.withArguments(
                new StringArgument("name"),
                new GreedyStringArgument("display"));
        this.executesPlayer(this);
    }

    @Override
    public void run(Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        String name = Objects.requireNonNull(args.getUnchecked("name"));
        if (!this.manager.getTagManager().isValidName(name)) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.tag.create.invalid-name", NamedTextColor.RED,
                    Component.text(TagManager.MAX_NAME_LENGTH))));
            return;
        }

        if (this.manager.getTagManager().resolveTagKey(name) != null) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.tag.create.name-taken", NamedTextColor.RED, Component.text(name))));
            return;
        }

        String input = Objects.requireNonNull(args.getUnchecked("display"));
        String display = this.manager.getTagManager().validateDisplay(input);
        if (display == null) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.tag.invalid-display", NamedTextColor.RED,
                    Component.text(TagManager.MAX_DISPLAY_LENGTH))));
            return;
        }

        this.manager.updateConfig(config -> config.getTags().put(name,
                new CaConfig.TagEntry(sender.getUniqueId(), display)));

        sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                "ca.command.tag.create.success", NamedTextColor.GREEN,
                Component.text(name), this.manager.getTagManager().renderDisplay(display))));
    }
}
