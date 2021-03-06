package tk.booky.craftattack.commands.teleport;
// Created by booky10 in CraftAttack (14:50 01.03.21)

import dev.jorel.commandapi.CommandAPICommand;

public class TeleportSubCommand extends CommandAPICommand {

    public TeleportSubCommand() {
        super("teleport");

        withPermission("craftattack.command.teleport");
        withAliases("tp");

        withSubcommand(new BedSubCommand());
        withSubcommand(new EndSubCommand());
        withSubcommand(new SpawnSubCommand());
    }
}