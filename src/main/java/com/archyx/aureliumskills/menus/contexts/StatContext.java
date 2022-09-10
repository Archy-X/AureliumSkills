package com.archyx.aureliumskills.menus.contexts;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.slate.context.ContextProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatContext implements ContextProvider<@NotNull Stat> {

    private final @NotNull AureliumSkills plugin;

    public StatContext(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable Stat parse(@NotNull String input) {
        return plugin.getStatRegistry().getStat(input);
    }

}
