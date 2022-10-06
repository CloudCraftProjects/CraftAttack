package dev.booky.craftattack.commands.admin.spawn;
// Created by booky10 in CraftAttack (21:56 05.10.22)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaBoundingBox;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
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
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        CaBoundingBox box = this.manager.getConfig().getSpawnConfig().getElytraBox();
        if (box == null) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.spawn.elytra-box.get.not-set", NamedTextColor.RED)));
            return;
        }

        box = (CaBoundingBox) box.clone().expand(0d, 0d, 0d, -1d, -1d, -1d);
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.admin.spawn.elytra-box.get.success", NamedTextColor.GREEN).args(
                Component.text(box.getBlockMinX()), Component.text(box.getBlockMinY()), Component.text(box.getBlockMinZ()),
                Component.text(box.getBlockMaxX()), Component.text(box.getBlockMaxY()), Component.text(box.getBlockMaxZ()),
                Component.text(box.getWorld().getKey().asString()))));
    }
}
