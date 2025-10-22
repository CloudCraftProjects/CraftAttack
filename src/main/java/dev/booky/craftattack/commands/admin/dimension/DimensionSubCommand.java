package dev.booky.craftattack.commands.admin.dimension;
// Created by booky10 in Kingdoms (20:34 26.08.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.key.Key;

public class DimensionSubCommand extends CommandAPICommand {

    public DimensionSubCommand(CaManager manager, String id, Key dimension) {
        super(id);
        super.withPermission("craftattack.command.admin." + id);

        super.withSubcommand(new DimensionWarpLocationGetCommand(manager, id, dimension));
        super.withSubcommand(new DimensionWarpLocationSetCommand(manager, id, dimension));
        super.withSubcommand(new DimensionActivationGetCommand(manager, id, dimension));
        super.withSubcommand(new DimensionActivationSetCommand(manager, id, dimension));
    }
}
