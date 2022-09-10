package com.archyx.aureliumskills.menus.contexts;

import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.slate.context.ContextProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class ManaAbilityContext implements ContextProvider<@NotNull MAbility> {

    @Override
    public @Nullable MAbility parse(@NotNull String input) {
        return MAbility.valueOf(input.toUpperCase(Locale.ROOT));
    }
}
