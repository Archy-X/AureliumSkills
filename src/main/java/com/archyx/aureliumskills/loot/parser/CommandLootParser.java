package com.archyx.aureliumskills.loot.parser;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.commands.CommandExecutor;
import com.archyx.aureliumskills.loot.Loot;
import com.archyx.aureliumskills.loot.builder.CommandLootBuilder;

import java.util.Locale;
import java.util.Map;

public class CommandLootParser extends LootParser {

    public CommandLootParser(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public Loot parse(Map<?, ?> map) {
        CommandLootBuilder builder = new CommandLootBuilder(plugin);

        if (map.containsKey("executor")) {
            builder.executor(CommandExecutor.valueOf(getString(map, "executor").toUpperCase(Locale.ROOT)));
        }

        return builder.command(getString(map, "command"))
                .message(parseMessage(map))
                .weight(parseWeight(map))
                .sources(parseSources(map))
                .xp(parseXp(map)).build();
    }
}
