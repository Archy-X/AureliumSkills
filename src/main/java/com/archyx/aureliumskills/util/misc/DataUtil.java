package com.archyx.aureliumskills.util.misc;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataUtil {

    public static @NotNull Object getElement(@NotNull Map<?, ?> map, @NotNull String key) {
        // Check if not null
        Object object = map.get(key);
        Objects.requireNonNull(object, "Reward/loot requires entry with key " + key);
        return object;
    }

    public static @NotNull String getString(@NotNull Map<?, ?> map, @NotNull String key) {
        Object object = getElement(map, key);
        if (!(object instanceof String)) {
            throw new IllegalArgumentException("Key " + key + " must have value of type String");
        }
        return (String) object;
    }

    public static double getDouble(@NotNull Map<?, ?> map, @NotNull String key) {
        Object object = getElement(map, key);
        if (object instanceof Double) {
            return (double) object;
        } else if (object instanceof Integer) {
            return (double) (Integer) object;
        } else {
            throw new IllegalArgumentException("Key " + key + " must have value of type double");
        }
    }

    public static int getInt(@NotNull Map<?, ?> map, @NotNull String key) {
        Object object = getElement(map, key);
        if (!(object instanceof Integer)) {
            throw new IllegalArgumentException("Key " + key + " must have value of type int");
        }
        return (int) object;
    }

    public static boolean getBoolean(@NotNull Map<?, ?> map, @NotNull String key) {
        Object object = getElement(map, key);
        if (!(object instanceof Boolean)) {
            throw new IllegalArgumentException("Key " + key + " must have value of type boolean");
        }
        return (boolean) object;
    }

    public static @NotNull List<@NotNull String> getStringList(@NotNull Map<?, ?> map, @NotNull String key) {
        Object object = getElement(map, key);
        if (!(object instanceof List)) {
            throw new IllegalArgumentException("Key " + key + " must have value of type string list");
        }
        return castStringList(object);
    }

    public static @NotNull List<@NotNull String> castStringList(@NotNull Object listObj) {
        List<?> unknownList = (List<?>) listObj;
        List<@NotNull String> stringList = new ArrayList<>();
        for (Object element : unknownList) {
            if (element instanceof String) {
                stringList.add((String) element);
            }
        }
        return stringList;
    }

    public static @NotNull Map<?, ?> getMap(@NotNull Map<?, ?> map, @NotNull String key) {
        Object object = getElement(map, key);
        if (!(object instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Key " + key + " must be a section map");
        }
        return (Map<?, ?>) object;
    }

    public static @NotNull List<@NotNull Map<?, ?>> getMapList(@NotNull Map<?, ?> map, @NotNull String key) {
        Object object = getElement(map, key);
        if (!(object instanceof List)) {
            throw new IllegalArgumentException("Key " + key + " must have value of type section map list");
        }
        List<?> unknownList = (List<?>) object;
        List<@NotNull Map<?, ?>> mapList = new ArrayList<>();
        for (Object element : unknownList) {
            if (element instanceof Map) {
                mapList.add((Map<?, ?>) element);
            }
        }
        return mapList;
    }

}
