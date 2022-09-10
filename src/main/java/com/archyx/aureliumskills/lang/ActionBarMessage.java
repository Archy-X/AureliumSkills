package com.archyx.aureliumskills.lang;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ActionBarMessage implements MessageKey {

    IDLE,
    XP,
    XP_REMOVED,
    MAXED,
    MAXED_REMOVED,
    ABILITY,
    BOSS_BAR_XP,
    BOSS_BAR_MAXED;

    @Override
    public @NotNull String getPath() {
        return "action_bar." + this.toString().toLowerCase(Locale.ENGLISH);
    }

}
