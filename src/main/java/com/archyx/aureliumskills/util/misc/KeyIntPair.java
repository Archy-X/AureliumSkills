package com.archyx.aureliumskills.util.misc;

import org.jetbrains.annotations.NotNull;

public class KeyIntPair {

    private final @NotNull String key;
    private int value;

    public KeyIntPair(@NotNull String key, int value) {
        this.key = key;
        this.value =value;
    }

    public @NotNull String getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
