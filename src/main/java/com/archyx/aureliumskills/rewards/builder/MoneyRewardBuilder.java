package com.archyx.aureliumskills.rewards.builder;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.rewards.MoneyReward;
import com.archyx.aureliumskills.rewards.Reward;
import org.jetbrains.annotations.NotNull;

public class MoneyRewardBuilder extends RewardBuilder {

    private double amount;

    public MoneyRewardBuilder(@NotNull AureliumSkills plugin) {
        super(plugin);
    }

    public @NotNull MoneyRewardBuilder amount(double amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public @NotNull Reward build() {
        return new MoneyReward(plugin, amount);
    }
}
