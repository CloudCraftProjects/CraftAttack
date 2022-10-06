package dev.booky.craftattack.commands.admin.launchplates;
// Created by booky10 in CraftAttack (22:42 05.10.22)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class LaunchPlateListCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public LaunchPlateListCommand(CaManager manager) {
        super("list");
        this.manager = manager;

        super.withPermission("craftattack.command.admin.launches.list");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        if (this.manager.getConfig().getLaunchPlates().isEmpty()) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.launches.list.none", NamedTextColor.RED)));
            return;
        }

        TextComponent.Builder builder = Component.text();
        builder.append(CaManager.getPrefix());
        builder.append(Component.translatable("ca.command.admin.launches.list.header", NamedTextColor.GREEN));
        builder.append(Component.newline());

        boolean deletePerms = sender.hasPermission("craftattack.command.admin.launches.delete");
        boolean firstExec = true;
        for (Location location : this.manager.getConfig().getLaunchPlates()) {
            if (!firstExec) {
                builder.append(Component.newline());
            } else {
                firstExec = false;
            }

            Component deleteComp;
            if (!deletePerms) {
                deleteComp = Component.empty();
            } else {
                String delCmd = "/minecraft:craftattack admin launches delete " +
                        location.getWorld().getKey().asString() + ' ' +
                        location.getBlockX() + ' ' +
                        location.getBlockY() + ' ' +
                        location.getBlockZ();

                deleteComp = Component.translatable("ca.command.admin.launches.list.delete.button", NamedTextColor.RED)
                        .hoverEvent(HoverEvent.showText(Component.translatable("ca.command.admin.launches.list.delete.warning", NamedTextColor.RED)))
                        .clickEvent(ClickEvent.runCommand(delCmd));
            }

            Component entryComp = Component.translatable("ca.command.admin.launches.list.entry", NamedTextColor.YELLOW).args(
                    Component.text(location.getBlockX()), Component.text(location.getBlockY()), Component.text(location.getBlockZ()),
                    Component.text(location.getWorld().getKey().asString()), deleteComp);
            builder.append(Component.space()).append(entryComp);
        }

        sender.sendMessage(builder.build());
    }
}
