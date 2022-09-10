package com.archyx.aureliumskills.menus.contexts;

import com.archyx.aureliumskills.ability.Ability;
import com.archyx.slate.context.ContextProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class AbilityContext implements ContextProvider<@NotNull Ability> {

    @Override
    public @Nullable Ability parse(@NotNull String input) {
        try {
            return Ability.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
