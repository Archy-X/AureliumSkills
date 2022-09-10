package com.archyx.aureliumskills.skills;

import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.mana.MAbility;
import com.google.common.collect.ImmutableList;

import java.util.Locale;
import java.util.function.Supplier;

public interface Skill {

    ImmutableList<Supplier<Ability>> getAbilities();

    String getDescription(Locale locale);

    String getDisplayName(Locale locale);

    MAbility getManaAbility();

    String name();

    @Override
    String toString();

    Ability getXpMultiplierAbility();

}
