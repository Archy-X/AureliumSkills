package com.archyx.aureliumskills.menus.contexts;

import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.slate.context.ContextProvider;

import java.util.Locale;

public class ManaAbilityContext implements ContextProvider<MAbility> {

    @Override
    public MAbility parse(String input) {
        return MAbility.valueOf(input.toUpperCase(Locale.ROOT));
    }
}
