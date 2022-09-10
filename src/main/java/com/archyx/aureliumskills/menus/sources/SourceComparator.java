package com.archyx.aureliumskills.menus.sources;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.source.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Locale;

public abstract class SourceComparator implements Comparator<@NotNull Source> {

    protected final AureliumSkills plugin;

    public SourceComparator(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public static class Ascending extends SourceComparator {

        public Ascending(AureliumSkills plugin) {
            super(plugin);
        }

        @Override
        public int compare(Source source1, Source source2) {
            return (int) (plugin.getSourceManager().getXp(source1) * 100) - (int) (plugin.getSourceManager().getXp(source2) * 100);
        }
    }

    public static class Descending extends SourceComparator {

        public Descending(AureliumSkills plugin) {
            super(plugin);
        }

        @Override
        public int compare(Source source1, Source source2) {
            return (int) (plugin.getSourceManager().getXp(source2) * 100) - (int) (plugin.getSourceManager().getXp(source1) * 100);
        }
    }

    public static class Alphabetical extends SourceComparator {

        private final @Nullable Locale locale;

        public Alphabetical(AureliumSkills plugin, @Nullable Locale locale) {
            super(plugin);
            this.locale = locale;
        }

        @Override
        public int compare(@NotNull Source source1, @NotNull Source source2) {
            return source1.getDisplayName(locale).compareTo(source2.getDisplayName(locale));
        }
    }

    public static class ReverseAlphabetical extends SourceComparator {

        private final @Nullable Locale locale;

        public ReverseAlphabetical(AureliumSkills plugin, @Nullable Locale locale) {
            super(plugin);
            this.locale = locale;
        }

        @Override
        public int compare(@NotNull Source source1, @NotNull Source source2) {
            return source2.getDisplayName(locale).compareTo(source1.getDisplayName(locale));
        }
    }

}
