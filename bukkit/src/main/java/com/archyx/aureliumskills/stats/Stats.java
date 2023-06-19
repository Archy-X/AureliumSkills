package com.archyx.aureliumskills.stats;

import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.StatMessage;

import java.util.Locale;

public enum Stats implements Stat {

	STRENGTH,
	HEALTH,
	REGENERATION,
	LUCK,
	WISDOM,
	TOUGHNESS,
	CRIT_CHANCE,
	CRIT_DAMAGE,
	SPEED;

	@Override
	public String getDisplayName(Locale locale) {
		return Lang.getMessage(StatMessage.valueOf(this.name() + "_NAME"), locale);
	}

	@Override
	public String getColor(Locale locale) {
		return Lang.getMessage(StatMessage.valueOf(this.name() + "_COLOR"), locale);
	}

	@Override
	public String getSymbol(Locale locale) {
		return Lang.getMessage(StatMessage.valueOf(this.name() + "_SYMBOL"), locale);
	}

	@Override
	public String getDescription(Locale locale) {
		return Lang.getMessage(StatMessage.valueOf(this.name() + "_DESC"), locale);
	}

}
