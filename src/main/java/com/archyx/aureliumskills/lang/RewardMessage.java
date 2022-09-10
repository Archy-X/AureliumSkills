package com.archyx.aureliumskills.lang;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum RewardMessage implements MessageKey{

    ITEM_DEFAULT_MENU_MESSAGE,
    ITEM_DEFAULT_MENU_MESSAGE_MULTIPLE,
    ITEM_DEFAULT_CHAT_MESSAGE,
    ITEM_DEFAULT_CHAT_MESSAGE_MULTIPLE;

    private String section;
    private String key;

    RewardMessage() {
        String[] split = toString().split("_", 2);
        if (split.length == 2) {
            this.section = split[0].toLowerCase(Locale.ROOT);
            this.key = split[1].toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public @NotNull String getPath() {
        if (section != null && key != null) {
            return "rewards." + section + "." + key;
        } else {
            return "rewards." + toString().toLowerCase(Locale.ROOT);
        }
    }
}
