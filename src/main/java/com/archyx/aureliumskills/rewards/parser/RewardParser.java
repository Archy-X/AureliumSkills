package com.archyx.aureliumskills.rewards.parser;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.rewards.Reward;
import com.archyx.aureliumskills.util.misc.Parser;

import java.util.Map;

public abstract class RewardParser extends Parser {

    protected final AureliumSkills plugin;

    public RewardParser(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public abstract Reward parse(Map<?, ?> map);

}
