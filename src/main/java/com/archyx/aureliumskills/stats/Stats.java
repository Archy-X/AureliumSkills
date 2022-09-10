package com.archyx.aureliumskills.stats;

import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.StatMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum Stats implements Stat {

	STRENGTH,
	HEALTH,
	REGENERATION,
	LUCK,
	WISDOM,
	TOUGHNESS;

	@Override
	public @NotNull String getDisplayName(@Nullable Locale locale) {
		return Lang.getMessage(StatMessage.valueOf(this.name() + "_NAME"), locale);
	}

	@Override
	public @NotNull String getColor(@Nullable Locale locale) {
		return Lang.getMessage(StatMessage.valueOf(this.name() + "_COLOR"), locale);
	}

	@Override
	public @NotNull String getSymbol(@Nullable Locale locale) {
		return Lang.getMessage(StatMessage.valueOf(this.name() + "_SYMBOL"), locale);
	}

	@Override
	public @NotNull String getDescription(@Nullable Locale locale) {
		return Lang.getMessage(StatMessage.valueOf(this.name() + "_DESC"), locale);
	}

}
