package com.archyx.aureliumskills.stats;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import org.bukkit.entity.Player;

public class StatLeveler {

	private final AureliumSkills plugin;

	public StatLeveler(AureliumSkills plugin) {
		this.plugin = plugin;
	}

	public void reloadStat(Player player, Stat stat) {
		if (stat == Stats.HEALTH) {
			plugin.getHealth().reload(player);
		} else if (stat == Stats.LUCK) {
			new Luck(plugin).reload(player);
		} else if (stat == Stats.WISDOM) {
			if (!OptionL.getBoolean(Option.WISDOM_ALLOW_OVER_MAX_MANA)) {
				PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
				if (playerData == null) return;
				if (playerData.getMana() > playerData.getMaxMana()) {
					playerData.setMana(playerData.getMaxMana());
				}
			}
		} else if (stat == Stats.SPEED) {
			new Speed(plugin).reload(player);
		}
	}
	
}
