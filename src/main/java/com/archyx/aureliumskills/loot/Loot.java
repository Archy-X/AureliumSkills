package com.archyx.aureliumskills.loot;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.source.Source;

import java.util.Set;

public abstract class Loot {

    protected final AureliumSkills plugin;
    protected final int weight;
    protected final String message;
    protected final double xp;
    protected final Set<Source> sources;

    public Loot(AureliumSkills plugin, int weight, String message, double xp, Set<Source> sources) {
        this.plugin = plugin;
        this.weight = weight;
        this.message = message;
        this.xp = xp;
        this.sources = sources;
    }

    public int getWeight() {
        return weight;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Gets the amount of Skill XP to reward
     * @return The amount of XP, -1 if not specified
     */
    public double getXp() {
        return xp;
    }

    public Set<Source> getSources() {
        return sources;
    }

}
