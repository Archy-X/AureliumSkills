package com.archyx.aureliumskills.modifier;

import com.archyx.aureliumskills.skills.Skill;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Multiplier {

    private final @NotNull String name;
    private final @Nullable Skill skill;
    private final double value; // The value represents the percent more XP gained

    public Multiplier(@NotNull String name, @Nullable Skill skill, double value) {
        this.name = name;
        this.value = value;
        this.skill = skill;
    }

    public @NotNull String getName() {
        return name;
    }

    public @Nullable Skill getSkill() {
        return skill;
    }

    public boolean isGlobal() {
        return skill == null;
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Multiplier that = (Multiplier) o;
        return Double.compare(that.value, value) == 0 && Objects.equals(name, that.name) && Objects.equals(skill, that.skill);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, skill, value);
    }
}
