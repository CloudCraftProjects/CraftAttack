package dev.booky.craftattack.commands.tag;
// Created by booky10 in CraftAttack (07.10.26)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class LegacyStatusCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CaManager manager;

    public LegacyStatusCommand(CaManager manager) {
        super("status");
        this.manager = manager;

        this.withPermission("craftattack.command.tag");
        this.withArguments(new GreedyStringArgument("status")
                .replaceSuggestions(ArgumentSuggestions.strings(_ ->
                        Stream.concat(
                                manager.getConfig().getTags().keySet().stream()
                                        .map(key -> key.toLowerCase(Locale.ROOT)),
                                Stream.of("none")).toArray(String[]::new))));
        this.executesPlayer(this);

        CommandAPI.unregister(this.getName(), true);
        this.register();
    }

    @Override
    public void run(Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        String input = Objects.requireNonNull(args.getUnchecked("status"));
        if ("none".equalsIgnoreCase(input)) {
            this.manager.getTagManager().clearTag(sender);
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.tag.clear.success", NamedTextColor.GREEN)));
            return;
        }

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
