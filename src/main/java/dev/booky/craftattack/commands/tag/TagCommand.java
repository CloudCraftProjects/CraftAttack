package dev.booky.craftattack.commands.tag;
// Created by booky10 in CraftAttack (07.10.26)

import dev.booky.craftattack.CaManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;

public class TagCommand extends CommandAPICommand {

    public TagCommand(CaManager manager) {
        super("tags");
        super.withPermission("craftattack.command.tags");

        this.withSubcommand(new TagListCommand(manager));
        this.withSubcommand(new TagInfoCommand(manager));
        this.withSubcommand(new TagAssignCommand(manager));
        this.withSubcommand(new TagClearCommand(manager));
        this.withSubcommand(new TagCreateCommand(manager));
        this.withSubcommand(new TagEditCommand(manager));
        this.withSubcommand(new TagDeleteCommand(manager));

        CommandAPI.unregister(this.getName(), true);
        this.register();
    }
}
