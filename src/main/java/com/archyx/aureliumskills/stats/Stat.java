package com.archyx.aureliumskills.stats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public interface Stat {

    @NotNull String getDisplayName(@Nullable Locale locale);

    @NotNull String getColor(@Nullable Locale locale);

    @NotNull String getSymbol(@Nullable Locale locale);

    @NotNull String getDescription(@Nullable Locale locale);

    @NotNull String name();

}
