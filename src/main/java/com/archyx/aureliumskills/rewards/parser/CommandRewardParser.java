package com.archyx.aureliumskills.rewards.parser;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.commands.CommandExecutor;
import com.archyx.aureliumskills.rewards.Reward;
import com.archyx.aureliumskills.rewards.builder.CommandRewardBuilder;

import java.util.Locale;
import java.util.Map;

public class CommandRewardParser extends RewardParser {

    public CommandRewardParser(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public Reward parse(Map<?, ?> map) {
        CommandRewardBuilder builder = new CommandRewardBuilder(plugin);

        CommandExecutor executor;
        if (map.containsKey("executor")) {
            executor = CommandExecutor.valueOf(getString(map, "executor").toUpperCase(Locale.ROOT));
        } else {
            executor = CommandExecutor.CONSOLE;
        }
        builder.executor(executor);

        String command = getString(map, "command");
        builder.command(command);

        if (map.containsKey("revert_command")) {
            builder.revertCommand(getString(map, "revert_command"));
        }

        if (map.containsKey("revert_executor")) {
            builder.revertExecutor(CommandExecutor.valueOf(getString(map, "revert_executor").toUpperCase(Locale.ROOT)));
        }

        if (map.containsKey("menu_message")) {
            builder.menuMessage(getString(map, "menu_message"));
        }

        if (map.containsKey("chat_message")) {
            builder.chatMessage(getString(map, "chat_message"));
        }

        if (map.containsKey("message")) {
            String message = getString(map, "message");
            builder.chatMessage(message).menuMessage(message);
        }

        return builder.build();
    }
    
}
