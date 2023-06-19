package com.archyx.aureliumskills.rewards;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.LevelerMessage;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.stats.StatLeveler;
import com.archyx.aureliumskills.util.text.TextUtil;
import org.bukkit.entity.Player;

import java.util.Locale;

public class StatReward extends Reward {

    private final Stat stat;
    private final double value;

    public StatReward(AureliumSkills plugin, Stat stat, double value) {
        super(plugin);
        this.stat = stat;
        this.value = value;
    }

    @Override
    public void giveReward(Player player, Skill skill, int level) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;

        playerData.addStatLevel(stat, value);
        new StatLeveler(plugin).reloadStat(player, stat);
    }

    public Stat getStat() {
        return stat;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String getMenuMessage(Player player, Locale locale, Skill skill, int level) {
        return TextUtil.replace(Lang.getMessage(MenuMessage.REWARDS_ENTRY, locale),
                "{color}", stat.getColor(locale),
                "{num}", Lang.formatStatLevel(stat, value),
                "{symbol}", stat.getSymbol(locale),
                "{stat}", stat.getDisplayName(locale));
    }

    @Override
    public String getChatMessage(Player player, Locale locale, Skill skill, int level) {
        return TextUtil.replace(Lang.getMessage(LevelerMessage.STAT_LEVEL, locale),
                "{color}", stat.getColor(locale),
                "{num}", Lang.formatStatLevel(stat, value),
                "{symbol}", stat.getSymbol(locale),
                "{stat}", stat.getDisplayName(locale));
    }

}
