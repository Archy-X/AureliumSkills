package com.archyx.aureliumskills.util.misc;

import java.util.List;
import java.util.Map;

public class Parser {

    protected Object getElement(Map<?, ?> map, String key) {
        return DataUtil.getElement(map, key);
    }

    protected Object getElementOrDefault(Map<?, ?> map, String key, Object def) {
        try {
            return getElement(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
    
    protected String getString(Map<?, ?> map, String key) {
        return DataUtil.getString(map, key);
    }

    protected String getStringOrDefault(Map<?, ?> map, String key, String def) {
        try {
            return getString(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
    
    protected double getDouble(Map<?, ?> map, String key) {
        return DataUtil.getDouble(map, key);
    }

    protected double getDoubleOrDefault(Map<?, ?> map, String key, double def) {
        try {
            return getDouble(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
    
    protected int getInt(Map<?, ?> map, String key) {
        return DataUtil.getInt(map, key);
    }

    protected int getIntOrDefault(Map<?, ?> map, String key, int def) {
        try {
            return getInt(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
    
    protected boolean getBoolean(Map<?, ?> map, String key) {
        return DataUtil.getBoolean(map, key);
    }

    protected boolean getBooleanOrDefault(Map<?, ?> map, String key, boolean def) {
        try {
            return getBoolean(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
    
    protected List<String> getStringList(Map<?, ?> map, String key) {
        return DataUtil.getStringList(map, key);
    }

    protected List<String> getStringListOrDefault(Map<?, ?> map, String key, List<String> def) {
        try {
            return getStringList(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    protected Map<?, ?> getMap(Map<?, ?> map, String key) {
        return DataUtil.getMap(map, key);
    }

    protected Map<?, ?> getMapOrDefault(Map<?, ?> map, String key, Map<?, ?> def) {
        try {
            return getMap(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    protected List<Map<?, ?>> getMapList(Map<?, ?> map, String key) {
        return DataUtil.getMapList(map, key);
    }

    protected List<Map<?, ?>> getMapListOrDefault(Map<?, ?> map, String key, List<Map<?, ?>> def) {
        try {
            return getMapList(map, key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

}
