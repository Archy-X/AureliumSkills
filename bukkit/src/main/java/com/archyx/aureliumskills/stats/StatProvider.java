package com.archyx.aureliumskills.stats;

import com.archyx.aureliumskills.AureliumSkills;
import org.bukkit.entity.Player;

public interface StatProvider {

    double getEffectiveValue(AureliumSkills plugin, Player player, double statLevel);

    String formatValue(AureliumSkills plugin, double value);

}
