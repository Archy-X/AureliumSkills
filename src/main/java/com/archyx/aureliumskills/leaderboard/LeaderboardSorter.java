package com.archyx.aureliumskills.leaderboard;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class LeaderboardSorter implements Comparator<@NotNull SkillValue>{

    @Override
    public int compare(@NotNull SkillValue a, @NotNull SkillValue b) {
        int levelA = a.getLevel();
        int levelB = b.getLevel();
        if (levelA != levelB) {
            return levelB - levelA;
        }
        else {
            return (int) (b.getXp() * 100) - (int) (a.getXp() * 100);
        }
    }

}
