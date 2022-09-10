package com.archyx.aureliumskills.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface MessageKey {

    @Nullable String getPath();

    static @NotNull Set<@NotNull MessageKey> values() {
        @NotNull Set<@NotNull MessageKey> keys = new HashSet<>(Arrays.asList(AbilityMessage.values()));
        keys.addAll(Arrays.asList(CommandMessage.values()));
        keys.addAll(Arrays.asList(MenuMessage.values()));
        keys.addAll(Arrays.asList(ManaAbilityMessage.values()));
        keys.addAll(Arrays.asList(SkillMessage.values()));
        keys.addAll(Arrays.asList(StatMessage.values()));
        keys.addAll(Arrays.asList(UnitMessage.values()));
        keys.addAll(Arrays.asList(ActionBarMessage.values()));
        keys.addAll(Arrays.asList(LevelerMessage.values()));
        keys.addAll(Arrays.asList(RewardMessage.values()));
        return keys;
    }
}
