package dev.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:50 01.03.21)

import dev.jorel.commandapi.CommandAPICommand;
import dev.booky.craftattack.utils.CraftAttackManager;

public class TeleportSubCommand extends CommandAPICommand {

    public TeleportSubCommand(CraftAttackManager manager) {
        super("teleport");
        withPermission("craftattack.command.teleport").withAliases("tp");

        withSubcommand(new BedSubCommand(manager));
        withSubcommand(new EndSubCommand(manager));
        withSubcommand(new SpawnSubCommand(manager));
    }
}
