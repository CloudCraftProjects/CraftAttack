package dev.booky.craftattack.commands.admin.launchplates;
// Created by booky10 in CraftAttack (22:40 05.10.22)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPICommand;

public class LaunchPlateSubCommand extends CommandAPICommand {

    public LaunchPlateSubCommand(CaManager manager) {
        super("launches");
        super.withPermission("craftattack.command.admin.launches");

        super.withSubcommand(new LaunchPlateListCommand(manager));
        super.withSubcommand(new LaunchPlateCreateCommand(manager, true));
        super.withSubcommand(new LaunchPlateCreateCommand(manager, false));
        super.withSubcommand(new LaunchPlateDeleteCommand(manager, true));
        super.withSubcommand(new LaunchPlateDeleteCommand(manager, false));
    }
}
