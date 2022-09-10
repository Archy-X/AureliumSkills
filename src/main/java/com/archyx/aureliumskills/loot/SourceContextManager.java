package com.archyx.aureliumskills.loot;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.source.SourceTag;
import com.archyx.aureliumskills.util.misc.DataUtil;
import com.archyx.lootmanager.loot.context.ContextManager;
import com.archyx.lootmanager.loot.context.LootContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SourceContextManager extends ContextManager {

    private final AureliumSkills plugin;

    public SourceContextManager(AureliumSkills plugin) {
        super("sources");
        this.plugin = plugin;
    }

    @Override
    public @Nullable Set<LootContext> parseContext(@NotNull Map<?, ?> parentMap) {
        Set<LootContext> contexts = new HashSet<>();
        if (parentMap.containsKey("sources")) {
            List<@NotNull String> sourcesList = DataUtil.getStringList(parentMap, "sources");
            for (String name : sourcesList) {
                Source source = plugin.getSourceRegistry().valueOf(name);
                if (source != null) {
                    contexts.add(source);
                } else {
                    SourceTag tag = SourceTag.valueOf(name.toUpperCase(Locale.ROOT));
                    @Nullable List<@NotNull Source> sourceList = plugin.getSourceManager().getTag(tag);
                    contexts.addAll(sourceList);
                }
            }
        }
        return contexts;
    }
}
