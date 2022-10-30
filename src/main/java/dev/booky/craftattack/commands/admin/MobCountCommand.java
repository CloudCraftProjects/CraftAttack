package dev.booky.craftattack.commands.admin;
// Created by booky10 in CraftAttack (20:54 30.10.22)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.MobCountUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntityTypeArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Map;

public class MobCountCommand extends CommandAPICommand implements PlayerCommandExecutor {

    public MobCountCommand() {
        super("mobcounts");
        super.withPermission("craftattack.command.admin.mobcounts");

        super.withArguments(new EntityTypeArgument("type"));
        super.executesPlayer(this);
    }

    @Override
    public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
        // TODO: translations
        EntityType type = (EntityType) args[0];
        Map<Location, Integer> counted = MobCountUtils.run(sender.getWorld(), type);

        if (counted.isEmpty()) {
            sender.sendMessage(CaManager.getPrefix().append(Component.text("No entities found with type " + type, NamedTextColor.RED)));
            return;
        }

        TextComponent.Builder builder = Component.text();
        counted.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
            if (!builder.children().isEmpty()) {
                builder.append(Component.newline());
            }

            String tpString = "/minecraft:execute in "
                    + entry.getKey().getWorld().getKey().asString()
                    + " run teleport @s "
                    + entry.getKey().getX() + " "
                    + entry.getKey().getY() + " "
                    + entry.getKey().getZ() + " "
                    + entry.getKey().getYaw() + " "
                    + entry.getKey().getPitch();

            builder.append(Component.text()
                    .clickEvent(ClickEvent.runCommand(tpString))
                    .append(Component.text("- "))
                    .append(Component.text(entry.getValue())).append(Component.text(" | "))
                    .append(Component.text(entry.getKey().getBlockX())).append(Component.text(";"))
                    .append(Component.text(entry.getKey().getBlockY())).append(Component.text(";"))
                    .append(Component.text(entry.getKey().getBlockZ())));
        });

        sender.sendMessage(builder.build());
    }
}
