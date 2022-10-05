package dev.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:50 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;

public class TeleportSubCommand extends CommandAPICommand {

    public TeleportSubCommand(CaManager manager) {
        super("teleport");
        withPermission("craftattack.command.teleport").withAliases("tp");

        withSubcommand(new BedSubCommand(manager));
        withSubcommand(new EndSubCommand(manager));
        withSubcommand(new SpawnSubCommand(manager));
    }
}
