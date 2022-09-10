package com.archyx.aureliumskills.modifier;

import com.archyx.aureliumskills.skills.Skill;

import java.util.Objects;

public class Multiplier {

    private final String name;
    private final Skill skill;
    private final double value; // The value represents the percent more XP gained

    public Multiplier(String name, Skill skill, double value) {
        this.name = name;
        this.value = value;
        this.skill = skill;
    }

    public String getName() {
        return name;
    }

    public Skill getSkill() {
        return skill;
    }

    public boolean isGlobal() {
        return skill == null;
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
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
