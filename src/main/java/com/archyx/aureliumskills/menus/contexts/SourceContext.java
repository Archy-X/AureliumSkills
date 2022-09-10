package com.archyx.aureliumskills.menus.contexts;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.slate.context.ContextProvider;

public class SourceContext implements ContextProvider<Source> {

    private final AureliumSkills plugin;

    public SourceContext(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public Source parse(String input) {
        return plugin.getSourceRegistry().valueOf(input);
    }
}
