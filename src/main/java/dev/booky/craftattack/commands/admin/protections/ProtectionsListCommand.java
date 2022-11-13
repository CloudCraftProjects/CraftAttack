package dev.booky.craftattack.commands.admin.protections;
// Created by booky10 in CraftAttack (22:42 05.10.22)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaBoundingBox;
import dev.booky.craftattack.utils.ProtectedArea;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class ProtectionsListCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public ProtectionsListCommand(CaManager manager) {
        super("list");
        this.manager = manager;

        super.withPermission("craftattack.command.admin.protections.list");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        if (this.manager.getConfig().getProtectedAreas().isEmpty()) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.protections.list.none", NamedTextColor.RED)));
            return;
        }

        TextComponent.Builder builder = Component.text();
        builder.append(CaManager.getPrefix());
        builder.append(Component.translatable("ca.command.admin.protections.list.header", NamedTextColor.GREEN));
        builder.append(Component.newline());

        boolean deletePerms = sender.hasPermission("craftattack.command.admin.protections.delete");
        boolean firstExec = true;
        for (ProtectedArea area : this.manager.getConfig().getProtectedAreas()) {
            CaBoundingBox box = (CaBoundingBox) area.getBox().expand(0d, 0d, 0d,
                    -1d, -1d, -1d);

            if (!firstExec) {
                builder.append(Component.newline());
            } else {
                firstExec = false;
            }

            Component deleteComp;
            if (!deletePerms) {
                deleteComp = Component.empty();
            } else {
                String delCmd = "/minecraft:craftattack admin protections delete " +
                        box.getWorld().getKey().asString() + ' ' +
                        box.getBlockMinX() + ' ' +
                        box.getBlockMinY() + ' ' +
                        box.getBlockMinZ() + ' ' +
                        box.getBlockMaxX() + ' ' +
                        box.getBlockMaxY() + ' ' +
                        box.getBlockMaxZ();

                deleteComp = Component.translatable("ca.command.admin.protections.list.delete.button", NamedTextColor.RED)
                        .hoverEvent(HoverEvent.showText(Component.translatable("ca.command.admin.protections.list.delete.warning", NamedTextColor.RED)))
                        .clickEvent(ClickEvent.runCommand(delCmd));
            }

            Component entryComp = Component.translatable("ca.command.admin.protections.list.entry", NamedTextColor.YELLOW).args(
                    Component.text(box.getBlockMinX()), Component.text(box.getBlockMinY()), Component.text(box.getBlockMinZ()),
                    Component.text(box.getBlockMaxX()), Component.text(box.getBlockMaxY()), Component.text(box.getBlockMaxZ()),
                    Component.text(box.getWorld().getKey().asString()), deleteComp);
            builder.append(Component.space()).append(entryComp);
        }

        sender.sendMessage(builder.build());
    }
}
