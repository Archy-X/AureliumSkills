package com.archyx.aureliumskills.configuration;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OptionValue {

    private @NotNull Object value;

    public OptionValue(@NotNull Object value) {
        this.value = value;
    }

    public void setValue(@NotNull Object value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public @NotNull Object getValue() {
        return value;
    }

    public int asInt() {
        return (int) value;
    }

    public double asDouble() {
        if (!(value instanceof Integer)) {
            return (double) value;
        }
        else {
            return ((Integer) value).doubleValue();
        }
    }

    public boolean asBoolean() {
        return (boolean) value;
    }

    public @NotNull String asString() {
        if (value instanceof String) {
            return (String) value;
        }
        else {
            return String.valueOf(value);
        }
    }

    public @NotNull List<@NotNull String> asList() {
        List<@NotNull String> stringList = new ArrayList<>();
        if (value instanceof List<?>) {
            for (Object obj : (List<?>) value) {
                if (obj instanceof String) {
                    stringList.add((String) obj);
                }
            }
        }
        return stringList;
    }

    public @NotNull ChatColor asColor() {
        if (value instanceof ChatColor) {
            return (ChatColor) value;
        }
        else {
            return ChatColor.valueOf(asString());
        }
    }
}
