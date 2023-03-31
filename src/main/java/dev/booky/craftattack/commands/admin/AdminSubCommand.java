package dev.booky.craftattack.commands.admin;
// Created by booky10 in CraftAttack (15:03 01.03.21)

import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.commands.admin.end.EndSubCommand;
import dev.booky.craftattack.commands.admin.protections.ProtectionsSubCommand;
import dev.booky.craftattack.commands.admin.spawn.SpawnSubCommand;
import dev.jorel.commandapi.CommandAPICommand;

public class AdminSubCommand extends CommandAPICommand {

    public AdminSubCommand(CaManager manager) {
        super("admin");
        super.withPermission("craftattack.command.admin");

        super.withSubcommand(new EndSubCommand(manager));
        super.withSubcommand(new SpawnSubCommand(manager));
        super.withSubcommand(new ProtectionsSubCommand(manager));
        super.withSubcommand(new ReloadConfigCommand(manager));
        super.withSubcommand(new MobCountCommand());
    }
}
