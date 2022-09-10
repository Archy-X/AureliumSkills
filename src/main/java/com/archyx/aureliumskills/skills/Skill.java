package com.archyx.aureliumskills.skills;

import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.mana.MAbility;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Supplier;

public interface Skill {

    @NotNull ImmutableList<@NotNull Supplier<@NotNull Ability>> getAbilities();

    @NotNull String getDescription(@Nullable Locale locale);

    @NotNull String getDisplayName(@Nullable Locale locale);

    @Nullable MAbility getManaAbility();

    @NotNull String name();

    @Override
    String toString();

    @NotNull Ability getXpMultiplierAbility();

}
