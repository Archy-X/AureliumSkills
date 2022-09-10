package com.archyx.aureliumskills.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MessageUpdates {

    LEVELER_FORMAT(9, "leveler", "The leveler section was changed to the new default messages due to significant format changes. If you had changed these messages before, you will need to change them again to follow the new format."),
    ABSORPTION(10, "mana_abilities.absorption", "The absorption messages were changed to match the mechanics in the new update"),
    LEVELER_STAT_LEVEL(18, "leveler.stat_level", "The message leveler.stat_level was reset to default to account for stat level up values potentially being different from 1 in the rewards update."),
    REPLENISH_DESC(21, "mana_abilities.speed_mine.desc", "The speed mine description message was reset to default for the haste_level option"),
    ABILITY_LEVELS(25, "menus.common.ability_levels", "The menus.common.ability_levels message was reset to fix a bug, this is normal."),
    STATS_LEVELED(25, "menus.common.stats_leveled", "The menus.common.stats_leveled message was reset to fix a bug, this is normal."),
    MANA_ABILITY(25, "menus.common.mana_ability", "The menus.common.mana_ability message was reset to fix a bug, this is normal."),
    BACK_CLICK(25, "menus.level_progression_menu.back_click", "The menus.level_progression_menu.back_click message was reset for the menus update, this is normal."),
    SHARP_HOOK_MENU(25, "mana_abilities.sharp_hook.menu", "The menu messages for the mana abilities sharp_hook, charged_shot, and lightning_blade were reset to fix a bug, this is normal"),
    CHARGED_SHOT_MENU(25, "mana_abilities.charged_shot.menu", null),
    LIGHTNING_BLADE_MENU(25, "mana_abilities.lightning_blade.menu", null);

    private final int version;
    private final @NotNull String path;
    private final @Nullable String message;

    MessageUpdates(int version, @NotNull String path, @Nullable String message) {
        this.version = version;
        this.path = path;
        this.message = message;
    }

    public int getVersion() {
        return version;
    }

    public @NotNull String getPath() {
        return path;
    }

    public @Nullable String getMessage() {
        return message;
    }

}
