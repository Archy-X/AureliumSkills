package com.archyx.aureliumskills.rewards.builder;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.rewards.Reward;
import com.archyx.aureliumskills.rewards.StatReward;
import com.archyx.aureliumskills.stats.Stat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StatRewardBuilder extends RewardBuilder {

    private @Nullable Stat stat;
    private double value;

    public StatRewardBuilder(@NotNull AureliumSkills plugin) {
        super(plugin);
        this.value = 1.0;
    }

    public @NotNull StatRewardBuilder stat(@NotNull Stat stat) {
        this.stat = stat;
        return this;
    }

    public @NotNull StatRewardBuilder value(double value) {
        this.value = value;
        return this;
    }

    @Override
    public @NotNull Reward build() {
        Objects.requireNonNull(stat, "You must specify a stat");
        return new StatReward(plugin, stat, value);
    }
}
