package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (07.10.26)

import com.google.common.collect.ImmutableSet;
import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.SuffixNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@NullMarked
public final class TagManager {

    public static final String MANAGE_PERMISSION = "craftattack.command.tag.manage";

    public static final int MAX_NAME_LENGTH = 32;
    public static final int MAX_DISPLAY_LENGTH = 32;
    private static final int MAX_DISPLAY_INPUT_LENGTH = 128;
    private static final int MAX_DISPLAY_SERIALIZED_LENGTH = 256;

    // only allow tags which don't contain clickable/executable content
    private static final MiniMessage TAG_FORMAT = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.gradient())
                    .resolver(StandardTags.rainbow())
                    .resolver(StandardTags.transition())
                    .resolver(StandardTags.reset())
                    .build())
            .build();

    private final CaManager manager;

    public TagManager(CaManager manager) {
        this.manager = manager;
    }

    private NodeMatcher<SuffixNode> suffixPredicate() {
        String server = this.manager.getConfig().getTagServerContext();
        return node -> node instanceof SuffixNode
                && node.getContexts().contains("server", server);
    }

    private SuffixNode buildSuffixNode(String display) {
        return SuffixNode.builder(display, 100)
                .withContext("server", this.manager.getConfig().getTagServerContext())
                .build();
    }

    private static @Nullable Component parseLenient(String input) {
        try {
            return MiniMessage.miniMessage().deserialize(input);
        } catch (ParsingException exception) {
            return null;
        }
    }

    public Argument<String> tagArgument(String nodeName) {
        return new StringArgument(nodeName)
                .replaceSuggestions(ArgumentSuggestions.strings(_ ->
                        this.manager.getConfig().getTags().keySet().stream()
                                .map(key -> key.toLowerCase(Locale.ROOT))
                                .toArray(String[]::new)));
    }

    public @Nullable String resolveTagKey(String input) {
        return this.manager.getConfig().getTags().keySet().stream()
                .filter(key -> key.equalsIgnoreCase(input))
                .findAny().orElse(null);
    }

    public boolean isValidName(String name) {
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            if (Character.isWhitespace(character) || Character.isISOControl(character)
                    || character == '<' || character == '>' || character == '\\'
                    || character == '"' || character == '\'') {
                return false;
            }
        }
        return true;
    }

    public @Nullable String validateDisplay(String input) {
        if (input.isEmpty() || input.length() > MAX_DISPLAY_INPUT_LENGTH) {
            return null;
        }

        Component parsed;
        try {
            parsed = TAG_FORMAT.deserialize(input);
        } catch (ParsingException exception) {
            return null;
        }

        String plain = PlainTextComponentSerializer.plainText().serialize(parsed);
        if (plain.isBlank() || plain.length() > MAX_DISPLAY_LENGTH) {
            return null;
        }

        String serialized = MiniMessage.miniMessage().serialize(parsed);
        if (serialized.length() > MAX_DISPLAY_SERIALIZED_LENGTH) {
            return null;
        }
        return serialized;
    }

    public Component renderDisplay(String display) {
        return TAG_FORMAT.deserialize(display);
    }

    public @Nullable String getCreatorName(@Nullable UUID creator) {
        return creator == null ? null : Bukkit.getOfflinePlayer(creator).getName();
    }

    public boolean canManage(Player player, CaConfig.TagEntry entry) {
        return player.hasPermission(MANAGE_PERMISSION)
                || (entry.getCreator() != null && entry.getCreator().equals(player.getUniqueId()));
    }

    public boolean assignTag(Player player, String key) {
        CaConfig.TagEntry entry = this.manager.getConfig().getTags().get(key);
        if (entry == null) {
            return false;
        }

        this.setSuffix(player.getUniqueId(), entry.getDisplay());
        return true;
    }

    public void clearTag(Player player) {
        this.clearSuffix(player.getUniqueId());
    }

    public CompletableFuture<Void> setSuffix(UUID uuid, String display) {
        Predicate<Node> predicate = this.suffixPredicate();
        return LuckPermsProvider.get().getUserManager().modifyUser(uuid, user -> {
            user.data().clear(predicate);
            user.data().add(this.buildSuffixNode(display));
        });
    }

    public CompletableFuture<Void> clearSuffix(UUID uuid) {
        Predicate<Node> predicate = this.suffixPredicate();
        return LuckPermsProvider.get().getUserManager().modifyUser(uuid,
                user -> user.data().clear(predicate));
    }

    public CompletableFuture<Set<UUID>> getAssignees(String display) {
        Component parsed = parseLenient(display);
        if (parsed == null) {
            return CompletableFuture.completedFuture(Set.of());
        }
        NodeMatcher<SuffixNode> predicate = this.suffixPredicate();
        return LuckPermsProvider.get().getUserManager()
                .searchAll(NodeMatcher.type(NodeType.SUFFIX))
                .thenApply(suffixes -> {
                    ImmutableSet.Builder<UUID> uuids = ImmutableSet.builder();
                    for (Map.Entry<UUID, Collection<SuffixNode>> entry : suffixes.entrySet()) {
                        for (SuffixNode suffix : entry.getValue()) {
                            if (!predicate.test(suffix)) {
                                continue;
                            }
                            Component suffixDisplay = parseLenient(suffix.getMetaValue());
                            if (parsed.equals(suffixDisplay)) {
                                uuids.add(entry.getKey());
                                break;
                            }
                        }
                    }
                    return uuids.build();
                });
    }
}
