package dev.booky.craftattack.commands.tag;
// Created by booky10 in CraftAttack (07.10.26)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.Objects;

public class TagInfoCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public TagInfoCommand(CaManager manager) {
        super("info");
        this.manager = manager;

        this.withPermission("craftattack.command.tag");
        this.withArguments(this.manager.getTagManager().tagArgument("tag"));
        this.executes(this);
    }

    @Override
    public void run(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        String input = Objects.requireNonNull(args.getUnchecked("tag"));
        Map<String, CaConfig.TagEntry> tags = this.manager.getConfig().getTags();
        String key = this.manager.getTagManager().resolveTagKey(input);
        if (key == null) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                    "ca.command.tag.invalid", NamedTextColor.RED, Component.text(input))));
            return;
        }

        CaConfig.TagEntry tag = tags.get(key);
        String creatorName = this.manager.getTagManager().getCreatorName(tag.getCreator());
        Component creator = creatorName == null
                ? Component.translatable("ca.command.tag.system", NamedTextColor.GRAY)
                : Component.text(creatorName);

        sender.sendMessage(CaManager.getPrefix().append(Component.translatable(
                "ca.command.tag.info.header", NamedTextColor.GREEN, Component.text(key))));
        sender.sendMessage(Component.translatable("ca.command.tag.info.display",
                this.manager.getTagManager().renderDisplay(tag.getDisplay())));
        sender.sendMessage(Component.translatable("ca.command.tag.info.creator", creator));
        this.manager.getTagManager().getAssignees(tag.getDisplay()).thenAccept(assignees ->
                Bukkit.getGlobalRegionScheduler().run(this.manager.getPlugin(), _ ->
                        sender.sendMessage(Component.translatable("ca.command.tag.info.assignees",
                                Component.text(assignees.size())))));
    }
}
