package dev.booky.craftattack.commands.admin;
// Created by booky10 in CraftAttack (20:06 28.10.2025)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.shops.ShopVillager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.NativeCommandExecutor;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SpawnShopCommand extends CommandAPICommand implements NativeCommandExecutor {

    private final CaManager manager;

    public SpawnShopCommand(CaManager manager) {
        super("spawnShop");
        this.manager = manager;

        this.withPermission("craftattack.command.admin.spawnshop");
        this.withArguments(new LocationArgument("target"));
        this.executesNative(this);
    }

    @Override
    public void run(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        Location target = args.<Location>getOptionalUnchecked("target").orElseThrow();
        ShopVillager.spawnShop(target, (LivingEntity) sender.getCallee(), this.manager.getPlugin());
    }
}
