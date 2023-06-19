package com.archyx.aureliumskills.stats;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.mechanics.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Strength implements StatProvider {

	public void strength(EntityDamageByEntityEvent event, PlayerData playerData, DamageType damageType) {
		if (damageType == DamageType.HAND) {
			if (OptionL.getBoolean(Option.STRENGTH_HAND_DAMAGE)) {
				applyStrength(event, playerData);
			}
		}
		else if (damageType == DamageType.BOW) {
			if (OptionL.getBoolean(Option.STRENGTH_BOW_DAMAGE)) {
				applyStrength(event, playerData);
			}
		}
		else {
			applyStrength(event, playerData);
		}
	}

	private void applyStrength(EntityDamageByEntityEvent event, PlayerData playerData) {
		double strength = playerData.getStatLevel(Stats.STRENGTH);
		if (OptionL.getBoolean(Option.STRENGTH_USE_PERCENT)) {
			event.setDamage(event.getDamage() * getDamageBooster(strength));
		} else {
			event.setDamage(event.getDamage() + getDamageBooster(strength));
		}
	}

	// Get the amount of damage multiplied or added depending on strength level
	private double getDamageBooster(double strength) {
		if (OptionL.getBoolean(Option.STRENGTH_USE_PERCENT)) { // Multiplier
			return 1 + (strength * OptionL.getDouble(Option.STRENGTH_MODIFIER)) / 100;
		} else { // Additive
			return strength * OptionL.getDouble(Option.STRENGTH_MODIFIER);
		}
	}

	@Override
	public double getEffectiveValue(AureliumSkills plugin, Player player, double statLevel) {
		if (!OptionL.getBoolean(Option.STRENGTH_SHOW_EFFECTIVE_VALUE)) {
			return statLevel;
		}
		double booster = getDamageBooster(statLevel);
		if (OptionL.getBoolean(Option.STRENGTH_USE_PERCENT)) {
			booster = (booster - 1.0) * 100;
		}
		if (OptionL.getBoolean(Option.STRENGTH_DISPLAY_DAMAGE_WITH_HEALTH_SCALING)) {
			booster *= OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING);
		}
		return booster;
	}

	@Override
	public String formatValue(AureliumSkills plugin, double value) {
		if (OptionL.getBoolean(Option.STRENGTH_USE_PERCENT)) {
			return "+" + NumberUtil.format1(value) + "%";
		} else {
			return "+" + NumberUtil.format1(value);
		}
	}
}
