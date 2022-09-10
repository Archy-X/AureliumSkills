package com.archyx.aureliumskills.rewards.builder;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.rewards.Reward;
import org.jetbrains.annotations.NotNull;

public abstract class RewardBuilder {

    protected final @NotNull AureliumSkills plugin;

    public RewardBuilder(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public abstract Reward build();

}
