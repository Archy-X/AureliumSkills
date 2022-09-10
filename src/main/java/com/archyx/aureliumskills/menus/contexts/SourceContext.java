package com.archyx.aureliumskills.menus.contexts;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.slate.context.ContextProvider;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class SourceContext implements ContextProvider<@NotNull Source> {

    private final AureliumSkills plugin;

    public SourceContext(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable Source parse(@NotNull String input) {
        return plugin.getSourceRegistry().valueOf(input);
    }
}
