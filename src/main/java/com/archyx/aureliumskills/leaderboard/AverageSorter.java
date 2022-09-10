package com.archyx.aureliumskills.leaderboard;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class AverageSorter implements Comparator<@NotNull SkillValue> {

    @Override
    public int compare(@NotNull SkillValue a, @NotNull SkillValue b) {
        return (int) (b.getXp() * 100) - (int) (a.getXp() * 100);
    }
}
