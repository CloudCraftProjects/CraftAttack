package dev.booky.craftattack.commands.admin.protections.flags;
// Created by booky10 in CraftAttack (19:55 14.11.22)

import com.google.common.base.Preconditions;
import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaBoundingBox;
import dev.booky.craftattack.utils.ProtectedArea;
import dev.booky.craftattack.utils.ProtectionFlag;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class FlagListCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public FlagListCommand(CaManager manager) {
        super("list");
        this.manager = manager;

        super.withArguments(new NamespacedKeyArgument("level")
                .includeSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getWorlds().stream()
                        .map(World::getKey).map(NamespacedKey::asString).toArray(String[]::new))));
        super.withArguments(new LocationArgument("corner1", LocationType.BLOCK_POSITION));
        super.withArguments(new LocationArgument("corner2", LocationType.BLOCK_POSITION));

        super.withPermission("craftattack.command.admin.protections.flag.list");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        World world = Preconditions.checkNotNull(Bukkit.getWorld((NamespacedKey) args[0]), "Unknown world " + args[0]);
        Location corner1 = (Location) args[1], corner2 = (Location) args[2];
        corner1.setWorld(world);
        corner2.setWorld(world);

        CaBoundingBox box = new CaBoundingBox(corner1, corner2);
        box.expand(0d, 0d, 0d, 1d, 1d, 1d);
        ProtectedArea area = new ProtectedArea(box);

        if (!this.manager.getConfig().getProtectedAreas().contains(area)) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.protections.flag.not-found", NamedTextColor.RED)));
            return;
        } else {
            area = this.manager.getConfig().getProtectedAreas().stream().filter(area::equals).findAny().orElseThrow();
        }

        if (area.getFlags().isEmpty()) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.protections.flag.list.no-flags", NamedTextColor.RED)));
            return;
        }

        String baseCmd = "/craftattack admin protections flag %s " +
                box.getWorld().getKey().asString() + ' ' +
                box.getBlockMinX() + ' ' +
                box.getBlockMinY() + ' ' +
                box.getBlockMinZ() + ' ' +
                (box.getBlockMaxX() - 1) + ' ' +
                (box.getBlockMaxY() - 1) + ' ' +
                (box.getBlockMaxZ() - 1);

        TextComponent.Builder builder = Component.text();
        builder.append(CaManager.getPrefix());
        builder.append(Component.translatable("ca.command.admin.protections.flag.list.header", NamedTextColor.GREEN));
        builder.append(Component.space());
        builder.append(Component.translatable("ca.command.admin.protections.flag.list.add.button", NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.translatable("ca.command.admin.protections.flag.list.add.info", NamedTextColor.AQUA)))
                .clickEvent(ClickEvent.suggestCommand(String.format(baseCmd, "add") + " ")));
        builder.append(Component.newline());

        boolean deletePerms = sender.hasPermission("craftattack.command.admin.protections.flag.remove");
        boolean firstExec = true;
        for (ProtectionFlag flag : area.getFlags()) {
            if (!firstExec) {
                builder.append(Component.newline());
            } else {
                firstExec = false;
            }

            Component deleteComp;
            if (!deletePerms) {
                deleteComp = Component.empty();
            } else {
                deleteComp = Component.translatable("ca.command.admin.protections.flag.list.delete.button", NamedTextColor.RED)
                        .hoverEvent(HoverEvent.showText(Component.translatable("ca.command.admin.protections.flag.list.delete.warning", NamedTextColor.RED)))
                        .clickEvent(ClickEvent.runCommand(String.format(baseCmd, "remove") + " " + flag.name()));
            }

            Component entryComp = Component.translatable("ca.command.admin.protections.flag.list.entry",
                    NamedTextColor.YELLOW).args(flag.getTranslation());
            builder
                    .append(Component.space()).append(entryComp)
                    .append(Component.space()).append(deleteComp);
        }

        sender.sendMessage(builder.build());
    }
}
