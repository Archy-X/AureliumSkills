package com.archyx.aureliumskills.stats;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.util.math.NumberUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StatRegistry {

    public final Map<String, Stat> stats;
    public final Map<Stat, StatProvider> providers;

    public StatRegistry() {
        this.stats = new HashMap<>();
        this.providers = new HashMap<>();
    }

    public void register(String key, Stat stat) {
        this.stats.put(key.toLowerCase(Locale.ROOT), stat);
    }

    public void registerProvider(Stat stat, StatProvider provider) {
        this.providers.put(stat, provider);
    }

    public Collection<Stat> getStats() {
        return stats.values();
    }

    @Nullable
    public Stat getStat(String key) {
        return this.stats.get(key.toLowerCase(Locale.ROOT));
    }

    @NotNull
    public StatProvider getProvider(Stat stat) {
        StatProvider provider = providers.get(stat);
        if (provider != null) {
            return provider;
        } else {
            return getDefaultStatProvider();
        }
    }

    @NotNull
    public <T extends StatProvider> T getProvider(Stat stat, Class<T> clazz) {
        StatProvider provider = getProvider(stat);
        if (clazz.isInstance(provider)) {
            return clazz.cast(provider);
        } else {
            throw new IllegalArgumentException("Stat provider for stat " + stat.toString() + " cannot be cast to class" + clazz.getName());
        }
    }

    public StatProvider getDefaultStatProvider() {
        return new StatProvider() { // Default stat provider
            @Override
            public double getEffectiveValue(AureliumSkills plugin, Player player, double statLevel) {
                return statLevel;
            }

            @Override
            public String formatValue(AureliumSkills plugin, double value) {
                return NumberUtil.format1(value);
            }
        };
    }

}
