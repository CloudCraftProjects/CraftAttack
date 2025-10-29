package dev.booky.craftattack.commands;
// Created by booky10 in CraftAttack (21:08 17.07.22)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.menu.MenuSlot;
import dev.booky.craftattack.menu.PagedMenu;
import dev.booky.craftattack.menu.context.MenuSlotsListContext;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class CraftCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private final CaManager manager;

    public CraftCommand(CaManager manager) {
        super("craft");
        this.manager = manager;
        super.withPermission("craftattack.command.craft");
        super.executesPlayer(this);

        // Additionally register just /craft as a command
        CommandAPI.unregister(this.getName(), true);
        super.register();
    }

    private static Consumer<MenuSlotsListContext> slots(int count) {
        return ctx -> {
            for (int i = 0; i < count; i++) {
                ctx.add(new MenuSlot(ItemStack.of(Material.STONE)));
            }
        };
    }

    @Override
    public void run(Player sender, CommandArguments args) throws WrapperCommandSyntaxException {

        PagedMenu.builder()
                .withTitle(MiniMessage.miniMessage().deserialize("<rainbow>test1"))
                .withSlots(slots(7))
                .withRows(1)
                .withParent(PagedMenu.builder()
                        .withTitle(MiniMessage.miniMessage().deserialize("<rainbow>test2"))
                        .withSlots(slots(8))
                        .withRows(1)
                        .withParent(PagedMenu.builder()
                                .withTitle(MiniMessage.miniMessage().deserialize("<rainbow>test3"))
                                .withSlots(slots(9))
                                .withRows(1)
                                .build())
                        .build())
                .open(sender);

        if (!sender.getInventory().contains(Material.CRAFTING_TABLE)) {
            sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.craft.no-table-found", NamedTextColor.RED)));
            return;
        }

        sender.openWorkbench(null, true);
        sender.sendMessage(CaManager.getPrefix().append(Component.translatable("ca.command.craft.success", NamedTextColor.GREEN)));
    }
}
