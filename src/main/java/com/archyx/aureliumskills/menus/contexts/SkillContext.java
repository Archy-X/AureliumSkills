package com.archyx.aureliumskills.menus.contexts;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.slate.context.ContextProvider;

public class SkillContext implements ContextProvider<Skill> {

    private final AureliumSkills plugin;

    public SkillContext(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public Skill parse(String input) {
        return plugin.getSkillRegistry().getSkill(input);
    }
}
