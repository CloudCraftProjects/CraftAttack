package dev.booky.craftattack.commands.admin.spawn;
// Created by booky10 in CraftAttack (21:56 05.10.22)

import dev.booky.cloudcore.util.BlockBBox;
import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class SpawnElytraBoxGetCommand extends CommandAPICommand implements CommandExecutor {

    private final CaManager manager;

    public SpawnElytraBoxGetCommand(CaManager manager) {
        super("elytra-box");
        this.manager = manager;

        super.withPermission("craftattack.command.admin.spawn.elytra-box.get");
        super.executes(this);
    }

    @Override
    public void run(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        BlockBBox box = this.manager.getConfig().getSpawnConfig().getElytraBox();
        if (box == null) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.spawn.elytra-box.get.not-set", NamedTextColor.RED)));
            return;
        }

        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.spawn.elytra-box.get.success", NamedTextColor.GREEN,
                Component.text(box.getMinX()), Component.text(box.getMinY()), Component.text(box.getMinZ()),
                Component.text(box.getMaxX()), Component.text(box.getMaxY()), Component.text(box.getMaxZ()),
                Component.text(box.getWorld().getKey().asString()))));
    }
}
