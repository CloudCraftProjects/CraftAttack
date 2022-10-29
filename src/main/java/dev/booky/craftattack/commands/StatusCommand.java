package dev.booky.craftattack.commands;
// Created by booky10 in CraftAttack (11:47 29.10.22)

import dev.booky.cloudchat.CloudChatApi;
import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.SuffixNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StatusCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private static final Predicate<Node> NODE_PREDICATE = node -> node instanceof SuffixNode && node.getContexts().contains("server", "craftattack");

    private final CloudChatApi chatApi;
    private final CaManager manager;

    public StatusCommand(CaManager manager) {
        super("status");
        this.chatApi = Bukkit.getServicesManager().load(CloudChatApi.class);
        this.manager = manager;

        super.withPermission("craftattack.command.status");
        super.executesPlayer(this);

        super.withArguments(new StringArgument("status")
                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                        Stream.concat(manager.getConfig().getStatuses().keySet().stream()
                                        .map(str -> str.toLowerCase(Locale.ROOT)),
                                Stream.of("none")).toArray(String[]::new))));

        // Additionally register just /status as a command
        super.register();
    }

    @Override
    public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
        String statusKey = args[0].toString().toLowerCase(Locale.ROOT);
        if ("none".equalsIgnoreCase(statusKey)) {
            LuckPermsProvider.get().getUserManager().modifyUser(sender.getUniqueId(), user -> {
                user.data().clear(NODE_PREDICATE);
                user.getCachedData().invalidate();

                sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                        "ca.command.status.success.remove",
                        NamedTextColor.GREEN)));

                Bukkit.getScheduler().runTaskLater(this.manager.getPlugin(),
                        () -> this.chatApi.updateTeam(sender), 10);
            });
            return;
        }

        String realKey = this.manager.getConfig().getStatuses().keySet().stream()
                .filter(key -> key.equalsIgnoreCase(statusKey))
                .findAny().orElse("null");
        Integer statusColor = this.manager.getConfig().getStatuses().get(realKey);

        if (statusColor == null) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.status.invalid-status", NamedTextColor.RED)));
            return;
        }

        Component suffix = Component.text(realKey, TextColor.color(statusColor));
        String serializedSuffix = LegacyComponentSerializer.legacySection().serialize(suffix);

        LuckPermsProvider.get().getUserManager().modifyUser(sender.getUniqueId(), user -> {
            Node suffixNode = SuffixNode.builder(serializedSuffix, 100)
                    .withContext("server", "craftattack")
                    .build();

            user.data().clear(NODE_PREDICATE);
            user.data().add(suffixNode);
            user.getCachedData().invalidate();

            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.status.success.update",
                    NamedTextColor.GREEN).args(suffix)));

            Bukkit.getScheduler().runTaskLater(this.manager.getPlugin(),
                    () -> this.chatApi.updateTeam(sender), 10);
        });
    }
}
