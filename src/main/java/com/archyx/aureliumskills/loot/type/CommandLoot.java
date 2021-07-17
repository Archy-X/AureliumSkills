package com.archyx.aureliumskills.loot.type;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.commands.CommandExecutor;
import com.archyx.aureliumskills.loot.Loot;
import com.archyx.aureliumskills.source.Source;

import java.util.Set;

public class CommandLoot extends Loot {

    private final CommandExecutor executor;
    private final String command;

    public CommandLoot(AureliumSkills plugin, int weight, String message, double xp, Set<Source> sources, CommandExecutor executor, String command) {
        super(plugin, weight, message, xp, sources);
        this.executor = executor;
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

}
