package tk.booky.craftattack.commands.admin;
// Created by booky10 in CraftAttack (15:03 01.03.21)

import dev.jorel.commandapi.CommandAPICommand;

public class AdminSubCommand extends CommandAPICommand {

    public AdminSubCommand() {
        super("admin");

        withPermission("craftattack.command.admin");

        withSubcommand(new GetSpawnRadiusSubCommand());
        withSubcommand(new SetSpawnRadiusSubCommand());

        withSubcommand(new GetEndLocationSubCommand());
        withSubcommand(new SetEndLocationSubCommand());

        withSubcommand(new GetSpawnLocationSubCommand());
        withSubcommand(new SetSpawnLocationSubCommand());
    }
}