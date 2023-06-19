package com.archyx.aureliumskills.lang;

import java.util.Locale;

public enum MenuMessage implements MessageKey {

    //Common
    CLOSE(0),
    LEVEL(0),
    PROGRESS_TO_LEVEL(0),
    MAX_LEVEL(0),
    ABILITY_LEVELS(0),
    ABILITY_LEVEL_ENTRY(0),
    ABILITY_LEVEL_ENTRY_LOCKED(0),
    STATS_LEVELED(0),
    MANA_ABILITY(0),
    //Skills Menu
    SKILLS_MENU_TITLE(1),
    YOUR_SKILLS(1),
    YOUR_SKILLS_DESC(1),
    YOUR_SKILLS_HOVER(1),
    YOUR_SKILLS_CLICK(1),
    SKILL_CLICK(1),
    SKILL_LOCKED(1),
    STATS(1),
    STATS_DESC(1),
    STATS_CLICK(1),
    //Level Progression Menu
    LEVEL_PROGRESSION_MENU_TITLE(2),
    YOUR_RANKING(2),
    RANK_OUT_OF(2),
    RANK_PERCENT(2),
    BACK(2),
    BACK_CLICK(2),
    NEXT_PAGE(2),
    NEXT_PAGE_CLICK(2),
    PREVIOUS_PAGE(2),
    PREVIOUS_PAGE_CLICK(2),
    LEVEL_UNLOCKED(2),
    LEVEL_IN_PROGRESS(2),
    LEVEL_LOCKED(2),
    LEVEL_NUMBER(2),
    UNLOCKED(2),
    IN_PROGRESS(2),
    LOCKED(2),
    REWARDS(2),
    REWARDS_ENTRY(2),
    MONEY_REWARD(2),
    ABILITY_UNLOCK(2),
    ABILITY_LEVEL(2),
    MANA_ABILITY_UNLOCK(2),
    MANA_ABILITY_LEVEL(2),
    PROGRESS(2),
    LEADERBOARD_CLICK(2),
    SOURCES(2),
    SOURCES_DESC(2),
    SOURCES_CLICK(2),
    ABILITIES(2),
    ABILITIES_DESC(2),
    ABILITIES_CLICK(2),
    //Stats Menu
    STATS_MENU_TITLE(3),
    PLAYER_STAT_ENTRY(3),
    SKILLS(3),
    YOUR_LEVEL(3),
    ATTACK_DAMAGE(3),
    HP(3),
    SATURATED_REGEN(3),
    FULL_HUNGER_REGEN(3),
    ALMOST_FULL_HUNGER_REGEN(3),
    MANA_REGEN(3),
    LUCK(3),
    DOUBLE_DROP_CHANCE(3),
    XP_GAIN(3),
    ANVIL_COST_REDUCTION(3),
    MAX_MANA(3),
    INCOMING_DAMAGE(3),
    CRIT_CHANCE(3),
    CRIT_DAMAGE(3),
    MOVEMENT_SPEED(3),
    // Unclaimed items menu
    UNCLAIMED_ITEMS_TITLE(4),
    INVENTORY_FULL(4),
    CLICK_TO_CLAIM(4),
    // Leaderboard menu
    LEADERBOARD_TITLE(5),
    PLAYER_ENTRY(5),
    SKILL_LEVEL(5),
    // Sources menu
    SOURCES_TITLE(6),
    SORTER(6),
    SORT_TYPE(6),
    SELECTED(6),
    DESCENDING(6),
    ASCENDING(6),
    ALPHABETICAL(6),
    REVERSE_ALPHABETICAL(6),
    SORT_CLICK(6),
    SOURCE_NAME(6),
    SOURCE_XP(6),
    SOURCE_XP_RATE(6),
    MULTIPLIED_XP(6),
    MULTIPLIED_XP_RATE(6),
    MULTIPLIED_DESC(6),
    // Abilities menu
    ABILITIES_TITLE(7),
    LOCKED_DESC(7),
    UNLOCKED_AT(7),
    YOUR_ABILITY_LEVEL(7),
    YOUR_ABILITY_LEVEL_MAXED(7),
    UNLOCKED_DESC(7),
    UNLOCKED_DESC_MAXED(7),
    DESC_UPGRADE_VALUE(7);

    private String path;
    
    MenuMessage(int section) {
        String key = this.name().toLowerCase(Locale.ENGLISH);
        if (section == 0) {
            this.path = "menus.common." + key;
        } else if (section == 1) {
            this.path = "menus.skills_menu." + key;
        } else if (section == 2) {
            this.path = "menus.level_progression_menu." + key;
        } else if (section == 3) {
            this.path = "menus.stats_menu." + key;
        } else if (section == 4) {
            this.path = "menus.unclaimed_items." + key;
        } else if (section == 5) {
            this.path = "menus.leaderboard." + key;
        } else if (section == 6) {
            this.path = "menus.sources." + key;
        } else if (section == 7) {
            this.path = "menus.abilities." + key;
        }
    }

    @Override
    public String getPath() {
        return path;
    }
}
