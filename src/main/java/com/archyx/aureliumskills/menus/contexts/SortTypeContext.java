package com.archyx.aureliumskills.menus.contexts;

import com.archyx.aureliumskills.menus.sources.SorterItem;
import com.archyx.slate.context.ContextProvider;

import java.util.Locale;

public class SortTypeContext implements ContextProvider<SorterItem.SortType> {

    @Override
    public SorterItem.SortType parse(String s) {
        return SorterItem.SortType.valueOf(s.toUpperCase(Locale.ROOT));
    }
}
