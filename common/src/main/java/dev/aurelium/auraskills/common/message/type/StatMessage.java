package dev.aurelium.auraskills.common.message.type;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.common.message.MessageKey;

import java.util.Locale;

public enum StatMessage implements MessageKey {
    
    STRENGTH_NAME,
    STRENGTH_DESC,
    STRENGTH_COLOR,
    STRENGTH_SYMBOL,
    HEALTH_NAME,
    HEALTH_DESC,
    HEALTH_COLOR,
    HEALTH_SYMBOL,
    REGENERATION_NAME,
    REGENERATION_DESC,
    REGENERATION_COLOR,
    REGENERATION_SYMBOL,
    LUCK_NAME,
    LUCK_DESC,
    LUCK_COLOR,
    LUCK_SYMBOL,
    WISDOM_NAME,
    WISDOM_DESC,
    WISDOM_COLOR,
    WISDOM_SYMBOL,
    TOUGHNESS_NAME,
    TOUGHNESS_DESC,
    TOUGHNESS_COLOR,
    TOUGHNESS_SYMBOL,
    CRIT_CHANCE_NAME,
    CRIT_CHANCE_DESC,
    CRIT_CHANCE_COLOR,
    CRIT_CHANCE_SYMBOL,
    CRIT_DAMAGE_NAME,
    CRIT_DAMAGE_DESC,
    CRIT_DAMAGE_COLOR,
    CRIT_DAMAGE_SYMBOL,
    ATTACK_SPEED_NAME,
    ATTACK_SPEED_DESC,
    ATTACK_SPEED_COLOR,
    ATTACK_SPEED_SYMBOL;
    
    private final Stat stat = Stats.valueOf(this.name().substring(0, this.name().lastIndexOf("_")));
    private final String path = "stats." + stat.name().toLowerCase(Locale.ROOT) + "." + this.toString().substring(this.name().lastIndexOf("_") + 1).toLowerCase(Locale.ROOT);
    
    @Override
    public String getPath() {
        return path;
    }
}