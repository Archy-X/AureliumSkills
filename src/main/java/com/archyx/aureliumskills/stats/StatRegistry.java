package com.archyx.aureliumskills.stats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StatRegistry {

    public final @NotNull Map<String, @NotNull Stat> stats;

    public StatRegistry() {
        this.stats = new HashMap<>();
    }

    public void register(@NotNull String key, @NotNull Stat stat) {
        this.stats.put(key.toLowerCase(Locale.ROOT), stat);
    }

    public @NotNull Collection<@NotNull Stat> getStats() {
        return stats.values();
    }

    public @Nullable Stat getStat(@NotNull String key) {
        return this.stats.get(key.toLowerCase(Locale.ROOT));
    }

}
