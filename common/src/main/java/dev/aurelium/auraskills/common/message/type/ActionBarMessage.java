package dev.aurelium.auraskills.common.message.type;

import dev.aurelium.auraskills.common.message.MessageKey;

import java.util.Locale;

public enum ActionBarMessage implements MessageKey {

    IDLE,
    XP,
    MAXED,
    ABILITY,
    BOSS_BAR_XP,
    BOSS_BAR_MAXED;

    @Override
    public String getPath() {
        return "action_bar." + this.toString().toLowerCase(Locale.ROOT);
    }

}
