package com.archyx.aureliumskills.menus.contexts;

import com.archyx.aureliumskills.menus.sources.SorterItem;
import com.archyx.slate.context.ContextProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SortTypeContext implements ContextProvider<SorterItem.@NotNull SortType> {

    @Override
    public SorterItem.@NotNull SortType parse(@NotNull String s) {
        return SorterItem.SortType.valueOf(s.toUpperCase(Locale.ROOT));
    }
}
