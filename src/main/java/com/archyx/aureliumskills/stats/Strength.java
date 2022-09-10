package com.archyx.aureliumskills.stats;

import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.util.mechanics.DamageType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class Strength {

	public void strength(@NotNull EntityDamageByEntityEvent event, @NotNull PlayerData playerData, DamageType damageType) {
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

	private void applyStrength(@NotNull EntityDamageByEntityEvent event, @NotNull PlayerData playerData) {
		double strength = playerData.getStatLevel(Stats.STRENGTH);
		if (OptionL.getBoolean(Option.STRENGTH_USE_PERCENT)) {
			event.setDamage(event.getDamage() * (1 + (strength * OptionL.getDouble(Option.STRENGTH_MODIFIER)) / 100));
		} else {
			event.setDamage(event.getDamage() + strength * OptionL.getDouble(Option.STRENGTH_MODIFIER));
		}
	}

}
