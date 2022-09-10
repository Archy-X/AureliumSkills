package com.archyx.aureliumskills.leaderboard;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SkillValue {

    private final @NotNull UUID id;
    private final int level;
    private final double xp;

    public SkillValue(@NotNull UUID id, int level, double xp) {
        this.id = id;
        this.level = level;
        this.xp = xp;
    }

    public @NotNull UUID getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public double getXp() {
        return xp;
    }

}
