package com.archyx.aureliumskills.rewards;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.skills.Skill;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class Reward {

    protected final @NotNull AureliumSkills plugin;

    public Reward(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public abstract void giveReward(@NotNull Player player, @NotNull Skill skill, int level);

    public abstract @NotNull String getMenuMessage(@NotNull Player player, @Nullable Locale locale, @NotNull Skill skill, int level);

    public abstract @NotNull String getChatMessage(@NotNull Player player, @Nullable Locale locale, @NotNull Skill skill, int level);

}
