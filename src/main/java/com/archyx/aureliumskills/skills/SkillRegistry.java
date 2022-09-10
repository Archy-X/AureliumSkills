package com.archyx.aureliumskills.skills;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SkillRegistry {

    public final @NotNull Map<String, @NotNull Skill> skills;

    public SkillRegistry() {
        this.skills = new HashMap<>();
    }

    public void register(@NotNull String key, @NotNull Skill skill) {
        this.skills.put(key.toLowerCase(Locale.ROOT), skill);
    }

    public @NotNull Collection<@NotNull Skill> getSkills() {
        return skills.values();
    }

    public @Nullable Skill getSkill(@NotNull String key) {
        return this.skills.get(key.toLowerCase(Locale.ROOT));
    }

}
