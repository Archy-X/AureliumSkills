package com.archyx.aureliumskills.stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StatRegistry {

    public final Map<String, Stat> stats;

    public StatRegistry() {
        this.stats = new HashMap<>();
    }

    public void register(String key, Stat stat) {
        this.stats.put(key.toLowerCase(Locale.ROOT), stat);
    }

    public Collection<Stat> getStats() {
        return stats.values();
    }

    public Stat getStat(String key) {
        return this.stats.get(key.toLowerCase(Locale.ROOT));
    }

}
