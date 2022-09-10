package com.archyx.aureliumskills.rewards.parser;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.rewards.Reward;
import com.archyx.aureliumskills.rewards.builder.PermissionRewardBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PermissionRewardParser extends RewardParser {
    
    public PermissionRewardParser(@NotNull AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Reward parse(@NotNull Map<?, ?> map) {
        PermissionRewardBuilder builder = new PermissionRewardBuilder(plugin);

        builder.permission(getString(map, "permission"));

        if (map.containsKey("value")) {
            builder.value(getBoolean(map, "value"));
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
