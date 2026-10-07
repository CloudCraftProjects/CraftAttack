package dev.booky.craftattack.commands.tag;
// Created by booky10 in CraftAttack (07.10.26)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TagListCommand extends CommandAPICommand implements CommandExecutor {

    private static final int ENTRIES_PER_PAGE = 10;

    private final CaManager manager;

    public TagListCommand(CaManager manager) {
        super("list");
        this.manager = manager;

        this.withPermission("craftattack.command.tag");
        this.withOptionalArguments(new IntegerArgument("page", 1));
        this.executes(this);
    }

    @Override
    public void run(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        Map<String, CaConfig.TagEntry> tags = this.manager.getConfig().getTags();
        int pages = Math.max(1, (tags.size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);
        int page = args.<Integer>getOptionalUnchecked("page").orElse(1);
        if (page < 1 || page > pages) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.tag.list.page-invalid", NamedTextColor.RED, Component.text(pages))));
            return;
        }

        if (tags.isEmpty()) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.tag.list.empty", NamedTextColor.RED)));
            return;
        }

        sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                "ca.command.tag.list.header", NamedTextColor.GREEN,
                Component.text(page), Component.text(pages))));

        List<Map.Entry<String, CaConfig.TagEntry>> entries = new ArrayList<>(tags.entrySet());
        int start = (page - 1) * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, entries.size());
        for (int i = start; i < end; i++) {
            Map.Entry<String, CaConfig.TagEntry> entry = entries.get(i);
            CaConfig.TagEntry tag = entry.getValue();

            String creatorName = this.manager.getTagManager().getCreatorName(tag.getCreator());
            Component creator = creatorName == null
                    ? Component.translatable("ca.command.tag.system", NamedTextColor.GRAY)
                    : Component.text(creatorName);

            sender.sendMessage(Component.translatable("ca.command.tag.list.entry",
                            Component.text(entry.getKey(), NamedTextColor.WHITE),
                            this.manager.getTagManager().renderDisplay(tag.getDisplay()), creator)
                    .clickEvent(ClickEvent.runCommand("/tags info " + entry.getKey()))
                    .hoverEvent(HoverEvent.showText(Component.translatable(
                            "ca.command.tag.list.entry.hover", NamedTextColor.GRAY))));
        }

        if (pages > 1) {
            Component footer = Component.empty();
            if (page > 1) {
                footer = footer.append(Component.translatable("ca.command.tag.list.previous", NamedTextColor.GRAY)
                        .clickEvent(ClickEvent.runCommand("/ca tag list " + (page - 1))));
            }
            if (page > 1 && page < pages) {
                footer = footer.append(Component.text(" | ", NamedTextColor.DARK_GRAY));
            }
            if (page < pages) {
                footer = footer.append(Component.translatable("ca.command.tag.list.next", NamedTextColor.GRAY)
                        .clickEvent(ClickEvent.runCommand("/ca tag list " + (page + 1))));
            }
            sender.sendMessage(footer);
        }
    }
}
