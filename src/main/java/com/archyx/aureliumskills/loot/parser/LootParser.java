package com.archyx.aureliumskills.loot.parser;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.loot.Loot;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.source.SourceTag;
import com.archyx.aureliumskills.util.misc.Parser;
import com.archyx.aureliumskills.util.text.TextUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class LootParser extends Parser {

    protected final AureliumSkills plugin;

    public LootParser(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public abstract Loot parse(Map<?, ?> map);

    protected int parseWeight(Map<?, ?> map) {
        if (map.containsKey("weight")) {
            return getInt(map, "weight");
        } else {
            return 10;
        }
    }

    protected String parseMessage(Map<?, ?> map) {
        if (map.containsKey("message")) {
            return TextUtil.replace(getString(map, "message"), "&", "§");
        } else {
            return "";
        }
    }

    @NotNull
    protected Set<Source> parseSources(Map<?, ?> map) {
        if (map.containsKey("sources")) {
            Set<Source> sources = new HashSet<>();
            for (String entry : getStringList(map, "sources")) {
                // Try get source name
                Source source = plugin.getSourceRegistry().valueOf(entry);
                if (source != null) {
                    sources.add(source);
                } else { // Try to get tag if not found
                    SourceTag tag = SourceTag.valueOf(entry.toUpperCase(Locale.ROOT));
                    // All all sources in tag
                    sources.addAll(plugin.getSourceManager().getTag(tag));
                }
            }
            return sources;
        } else {
            return new HashSet<>();
        }
    }

    protected double parseXp(Map<?, ?> map) {
        if (map.containsKey("xp")) {
            return getDouble(map, "xp");
        } else {
            return -1.0;
        }
    }

}
