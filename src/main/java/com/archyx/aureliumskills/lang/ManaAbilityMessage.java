package com.archyx.aureliumskills.lang;

import com.archyx.aureliumskills.mana.MAbility;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ManaAbilityMessage implements MessageKey {
    
    NONE,
    REPLENISH_NAME,
    REPLENISH_DESC,
    REPLENISH_RAISE,
    REPLENISH_LOWER,
    REPLENISH_START,
    REPLENISH_END,
    TREECAPITATOR_NAME,
    TREECAPITATOR_DESC,
    TREECAPITATOR_RAISE,
    TREECAPITATOR_LOWER,
    TREECAPITATOR_START,
    TREECAPITATOR_END,
    SPEED_MINE_NAME,
    SPEED_MINE_DESC,
    SPEED_MINE_RAISE,
    SPEED_MINE_LOWER,
    SPEED_MINE_START,
    SPEED_MINE_END,
    SHARP_HOOK_NAME,
    SHARP_HOOK_DESC,
    SHARP_HOOK_USE,
    SHARP_HOOK_MENU,
    TERRAFORM_NAME,
    TERRAFORM_DESC,
    TERRAFORM_RAISE,
    TERRAFORM_LOWER,
    TERRAFORM_START,
    TERRAFORM_END,
    CHARGED_SHOT_NAME,
    CHARGED_SHOT_DESC,
    CHARGED_SHOT_ENABLE,
    CHARGED_SHOT_DISABLE,
    CHARGED_SHOT_SHOOT,
    CHARGED_SHOT_MENU,
    ABSORPTION_NAME,
    ABSORPTION_DESC,
    ABSORPTION_RAISE,
    ABSORPTION_LOWER,
    ABSORPTION_START,
    ABSORPTION_END,
    LIGHTNING_BLADE_NAME,
    LIGHTNING_BLADE_DESC,
    LIGHTNING_BLADE_RAISE,
    LIGHTNING_BLADE_LOWER,
    LIGHTNING_BLADE_START,
    LIGHTNING_BLADE_END,
    LIGHTNING_BLADE_MENU,
    NOT_READY("not_ready"),
    NOT_ENOUGH_MANA("not_enough_mana");

    private final @NotNull String path;

    ManaAbilityMessage() {
        if (this.name().equals("NONE")) {
            this.path = "";
            return;
        }
        MAbility manaAbility = MAbility.valueOf(this.name().substring(0, this.name().lastIndexOf("_")));
        this.path = "mana_abilities." + manaAbility.name().toLowerCase(Locale.ENGLISH) + "." + this.name().substring(this.name().lastIndexOf("_") + 1).toLowerCase(Locale.ENGLISH);
    }

    ManaAbilityMessage(@NotNull String path) {
        this.path = "mana_abilities." + path;
    }

    @Override
    public @NotNull String getPath() {
        return path;
    }
}
