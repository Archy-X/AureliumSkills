package com.archyx.aureliumskills.rewards.parser;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.rewards.Reward;
import com.archyx.aureliumskills.util.misc.Parser;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class RewardParser extends Parser {

    protected final @NotNull AureliumSkills plugin;

    public RewardParser(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public abstract @NotNull Reward parse(@NotNull Map<?, ?> map);

}
