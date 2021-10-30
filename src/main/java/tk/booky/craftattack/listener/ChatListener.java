package tk.booky.craftattack.listener;
// Created by booky10 in CraftAttack (23:37 30.10.21)

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class ChatListener implements Listener, ChatRenderer.ViewerUnaware {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final UserManager USERS = LuckPermsProvider.get().getUserManager();
    private static final Component SEPERATOR = text()
        .append(space())
        .append(text('\u25cf', DARK_GRAY))
        .append(space())
        .build();

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.renderer(ChatRenderer.viewerUnaware(this));
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message) {
        User user = USERS.getUser(source.getUniqueId());
        TextComponent.Builder nameBuilder = text();

        if (user != null) {
            String prefixString = user.getCachedData().getMetaData().getPrefix();
            if (prefixString != null) {
                nameBuilder.append(SERIALIZER.deserialize(prefixString)).append(SEPERATOR);
            }
        }

        nameBuilder.append(sourceDisplayName.color(GRAY));
        return translatable("chat.type.text", nameBuilder.build(), message);
    }
}
