package com.archyx.aureliumskills.menus.contexts;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.slate.context.ContextProvider;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class SkillContext implements ContextProvider<@NotNull Skill> {

    private final @NotNull AureliumSkills plugin;

    public SkillContext(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable Skill parse(@NotNull String input) {
        return plugin.getSkillRegistry().getSkill(input);
    }
}
