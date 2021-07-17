package com.archyx.aureliumskills.loot.builder;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.commands.CommandExecutor;
import com.archyx.aureliumskills.loot.Loot;
import com.archyx.aureliumskills.loot.type.CommandLoot;

public class CommandLootBuilder extends LootBuilder {

    private CommandExecutor executor;
    private String command;

    public CommandLootBuilder(AureliumSkills plugin) {
        super(plugin);
        this.executor = CommandExecutor.CONSOLE;
    }

    @Override
    public Loot build() {
        return new CommandLoot(plugin, weight, message, xp, sources, executor, command);
    }

    public CommandLootBuilder executor(CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    public CommandLootBuilder command(String command) {
        this.command = command;
        return this;
    }

}
