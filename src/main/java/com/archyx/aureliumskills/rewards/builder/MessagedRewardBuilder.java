package com.archyx.aureliumskills.rewards.builder;

import com.archyx.aureliumskills.AureliumSkills;
import org.jetbrains.annotations.NotNull;

public abstract class MessagedRewardBuilder extends RewardBuilder {

    protected @NotNull String menuMessage;
    protected @NotNull String chatMessage;

    public MessagedRewardBuilder(@NotNull AureliumSkills plugin) {
        super(plugin);
        this.menuMessage = "";
        this.chatMessage = "";
    }

    public @NotNull MessagedRewardBuilder menuMessage(@NotNull String menuMessage) {
        this.menuMessage = menuMessage;
        return this;
    }

    public @NotNull MessagedRewardBuilder chatMessage(@NotNull String chatMessage) {
        this.chatMessage = chatMessage;
        return this;
    }

}
