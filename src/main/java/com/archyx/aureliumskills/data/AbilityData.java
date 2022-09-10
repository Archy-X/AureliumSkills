package com.archyx.aureliumskills.data;

import com.archyx.aureliumskills.ability.AbstractAbility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AbilityData {

    private final @NotNull AbstractAbility ability;
    private final @NotNull Map<@NotNull String, Object> data;

    public AbilityData(@NotNull AbstractAbility ability) {
        this.ability = ability;
        this.data = new HashMap<>();
    }

    public @NotNull AbstractAbility getAbility() {
        return ability;
    }

    public @Nullable Object getData(String key) {
        return data.get(key);
    }

    public @NotNull Map<@NotNull String, Object> getDataMap() {
        return data;
    }

    public void setData(@NotNull String key, @NotNull Object value) {
        this.data.put(key, value);
    }

    /**
     * Gets an ability data value as an int
     * @param key The key of the data to look up
     * @return The value as an int, or 0 if no mapping exists
     */
    public int getInt(@NotNull String key) {
        Object o = data.get(key);
        return o != null ? (int) o : 0;
    }

    /**
     * Gets an ability data value as a boolean
     * @param key The key of the data to look up
     * @return The value as a boolean, or false if no mapping exists
     */
    public boolean getBoolean(@NotNull String key) {
        Object o = data.get(key);
        return o != null && (boolean) o;
    }

    /**
     * Gets an ability data value as a double
     * @param key The key of the data to look up
     * @return The value as a double, or 0.0 if no mapping exists
     */
    public double getDouble(@NotNull String key) {
        Object o = data.get(key);
        if (o != null) {
            if (o instanceof Integer) {
                return (int) o;
            } else if (o instanceof Double) {
                return (double) o;
            }
        }
        return 0.0;
    }

    public boolean containsKey(@NotNull String key) {
        return data.containsKey(key);
    }

}
