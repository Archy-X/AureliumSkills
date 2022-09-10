package com.archyx.aureliumskills.util.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Parser {

    protected @NotNull Object getElement(@NotNull Map<?, ?> map, @NotNull String key) {
        return DataUtil.getElement(map, key);
    }

    protected @Nullable Object getElementOrDefault(@NotNull Map<?, ?> map, @NotNull String key, @Nullable Object def) {
        try {
            return getElement(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
    
    protected @NotNull String getString(@NotNull Map<?, ?> map, @NotNull String key) {
        return DataUtil.getString(map, key);
    }

    protected @Nullable String getStringOrDefault(@NotNull Map<?, ?> map, @NotNull String key, @Nullable String def) {
        try {
            return getString(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
    
    protected double getDouble(@NotNull Map<?, ?> map, @NotNull String key) {
        return DataUtil.getDouble(map, key);
    }

    protected double getDoubleOrDefault(@NotNull Map<?, ?> map, @NotNull String key, double def) {
        try {
            return getDouble(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
    
    protected int getInt(@NotNull Map<?, ?> map, @NotNull String key) {
        return DataUtil.getInt(map, key);
    }

    protected int getIntOrDefault(@NotNull Map<?, ?> map, @NotNull String key, int def) {
        try {
            return getInt(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
    
    protected boolean getBoolean(@NotNull Map<?, ?> map, @NotNull String key) {
        return DataUtil.getBoolean(map, key);
    }

    protected boolean getBooleanOrDefault(@NotNull Map<?, ?> map, @NotNull String key, boolean def) {
        try {
            return getBoolean(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
    
    protected @NotNull List<@NotNull String> getStringList(@NotNull Map<?, ?> map, @NotNull String key) {
        return DataUtil.getStringList(map, key);
    }

    protected @NotNull List<@NotNull String> getStringListOrDefault(@NotNull Map<?, ?> map, @NotNull String key, @NotNull List<@NotNull String> def) {
        try {
            return getStringList(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    protected @NotNull Map<?, ?> getMap(@NotNull Map<?, ?> map, @NotNull String key) {
        return DataUtil.getMap(map, key);
    }

    protected @NotNull Map<?, ?> getMapOrDefault(@NotNull Map<?, ?> map, @NotNull String key, @NotNull Map<?, ?> def) {
        try {
            return getMap(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    protected @NotNull List<@NotNull Map<?, ?>> getMapList(@NotNull Map<?, ?> map, @NotNull String key) {
        return DataUtil.getMapList(map, key);
    }

    protected @NotNull List<@NotNull Map<?, ?>> getMapListOrDefault(@NotNull Map<?, ?> map, @NotNull String key, @NotNull List<@NotNull Map<?, ?>> def) {
        try {
            return getMapList(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

}
