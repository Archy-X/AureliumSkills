package com.archyx.aureliumskills.loot;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.source.SourceTag;
import com.archyx.aureliumskills.util.misc.DataUtil;
import com.archyx.lootmanager.loot.context.ContextManager;
import com.archyx.lootmanager.loot.context.LootContext;

import java.util.*;

public class SourceContextManager extends ContextManager {

    private final AureliumSkills plugin;

    public SourceContextManager(AureliumSkills plugin) {
        super("sources");
        this.plugin = plugin;
    }

    @Override
    public Set<LootContext> parseContext(Map<?, ?> parentMap) {
        Set<LootContext> contexts = new HashSet<>();
        if (parentMap.containsKey("sources")) {
            List<String> sourcesList = DataUtil.getStringList(parentMap, "sources");
            for (String name : sourcesList) {
                Source source = plugin.getSourceRegistry().valueOf(name);
                if (source != null) {
                    contexts.add(source);
                } else {
                    SourceTag tag = SourceTag.valueOf(name.toUpperCase(Locale.ROOT));
                    List<Source> sourceList = plugin.getSourceManager().getTag(tag);
                    contexts.addAll(sourceList);
                }
            }
        }
        return contexts;
    }
}
