package com.archyx.aureliumskills.lang;

public enum MessageUpdates {

    LEVELER_FORMAT(9, "leveler", "The leveler section was changed to the new default messages due to significant format changes. If you had changed these messages before, you will need to change them again to follow the new format."),
    ABSORPTION(10, "mana_abilities.absorption", "The absorption messages were changed to match the mechanics in the new update"),
    LEVELER_STAT_LEVEL(18, "leveler.stat_level", "The message leveler.stat_level was reset to default to account for stat level up values potentially being different from 1 in the rewards update."),
    REPLENISH_DESC(21, "mana_abilities.speed_mine.desc", "The speed mine description message was reset to default for the haste_level option");

    private final int version;
    private final String path;
    private final String message;

    MessageUpdates(int version, String path, String message) {
        this.version = version;
        this.path = path;
        this.message = message;
    }

    public int getVersion() {
        return version;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

}
