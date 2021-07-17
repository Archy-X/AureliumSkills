package com.archyx.aureliumskills.util.misc;

import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataUtil {

    public static Object getElement(Map<?, ?> map, String key) {
        // Check if not null
        Object object = map.get(key);
        Validate.notNull(object, "Reward/loot requires entry with key " + key);
        return object;
    }

    public static String getString(Map<?, ?> map, String key) {
        Object object = getElement(map, key);
        if (!(object instanceof String)) {
            throw new IllegalArgumentException("Key " + key + " must have value of type String");
        }
        return (String) object;
    }

    public static double getDouble(Map<?, ?> map, String key) {
        Object object = getElement(map, key);
        if (object instanceof Double) {
            return (double) object;
        } else if (object instanceof Integer) {
            return (double) (Integer) object;
        } else {
            throw new IllegalArgumentException("Key " + key + " must have value of type double");
        }
    }

    public static int getInt(Map<?, ?> map, String key) {
        Object object = getElement(map, key);
        if (!(object instanceof Integer)) {
            throw new IllegalArgumentException("Key " + key + " must have value of type int");
        }
        return (int) object;
    }

    public static boolean getBoolean(Map<?, ?> map, String key) {
        Object object = getElement(map, key);
        if (!(object instanceof Boolean)) {
            throw new IllegalArgumentException("Key " + key + " must have value of type boolean");
        }
        return (boolean) object;
    }

    public static List<String> getStringList(Map<?, ?> map, String key) {
        Object object = getElement(map, key);
        if (!(object instanceof List)) {
            throw new IllegalArgumentException("Key " + key + " must have value of type string list");
        }
        List<?> unknownList = (List<?>) object;
        List<String> stringList = new ArrayList<>();
        for (Object element : unknownList) {
            if (element instanceof String) {
                stringList.add((String) element);
            }
        }
        return stringList;
    }

    public static Map<?, ?> getMap(Map<?, ?> map, String key) {
        Object object = getElement(map, key);
        if (!(object instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Key " + key + " must be a section map");
        }
        return (Map<?, ?>) object;
    }

    public static List<Map<?, ?>> getMapList(Map<?, ?> map, String key) {
        Object object = getElement(map, key);
        if (!(object instanceof List)) {
            throw new IllegalArgumentException("Key " + key + " must have value of type section map list");
        }
        List<?> unknownList = (List<?>) object;
        List<Map<?, ?>> mapList = new ArrayList<>();
        for (Object element : unknownList) {
            if (element instanceof Map) {
                mapList.add((Map<?, ?>) element);
            }
        }
        return mapList;
    }

}
