package com.archyx.aureliumskills.rewards;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.skills.Skill;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class MoneyReward extends Reward {

    private final double amount;

    public MoneyReward(@NotNull AureliumSkills plugin, double amount) {
        super(plugin);
        this.amount = amount;
    }

    @Override
    public void giveReward(@NotNull Player player, @NotNull Skill skill, int level) {
        if (plugin.isVaultEnabled()) {
            plugin.getEconomy().depositPlayer(player, amount);
        }
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public @NotNull String getMenuMessage(@NotNull Player player, Locale locale, @NotNull Skill skill, int level) {
        return ""; // All money rewards have to be added into one line
    }

    @Override
    public @NotNull String getChatMessage(@NotNull Player player, Locale locale, @NotNull Skill skill, int level) {
        return ""; // ALl money rewards have to be added into one line
    }
}
