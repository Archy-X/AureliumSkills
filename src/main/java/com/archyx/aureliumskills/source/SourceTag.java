package com.archyx.aureliumskills.source;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;

import java.util.Locale;

public enum SourceTag {

    BOUNTIFUL_HARVEST_APPLICABLE(Skills.FARMING),
    TRIPLE_HARVEST_APPLICABLE(Skills.FARMING),
    LUMBERJACK_APPLICABLE(Skills.FORAGING),
    LUCKY_MINER_APPLICABLE(Skills.MINING),
    SPEED_MINE_APPLICABLE(Skills.MINING),
    DIRTS(Skills.EXCAVATION),
    BIGGER_SCOOP_APPLICABLE(Skills.EXCAVATION),
    METAL_DETECTOR_APPLICABLE(Skills.EXCAVATION),
    LUCKY_SPADES_APPLICABLE(Skills.EXCAVATION),
    TERRAFORM_APPLICABLE(Skills.EXCAVATION);

    private final Skill skill;

    SourceTag(Skill skill) {
        this.skill = skill;
    }

    public Skill getSkill() {
        return skill;
    }

    public String getPath() {
        return skill.toString().toLowerCase(Locale.ROOT) + "." + toString().toLowerCase(Locale.ROOT);
    }

}
