package com.archyx.aureliumskills.modifier;

import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.util.math.NumberUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class StatModifier {

    private final @NotNull String name;
    private final @NotNull Stat stat;
    private final double value;

    public StatModifier(@NotNull String name, @NotNull Stat stat, double value) {
        this.name = name;
        this.stat = stat;
        this.value = value;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Stat getStat() {
        return stat;
    }

    public double getValue() {
        return value;
    }

    public static @NotNull String applyPlaceholders(@NotNull String input, @NotNull StatModifier modifier, Locale locale) {
        Stat stat = modifier.getStat();
        double value = modifier.getValue();
        String name = modifier.getName();
        return input.replace("{color}", stat.getColor(locale))
                .replace("{symbol}", stat.getSymbol(locale))
                .replace("{stat}", stat.getDisplayName(locale))
                .replace("{value}", NumberUtil.format1(value))
                .replace("{name}", name);
    }

    public static @NotNull String applyPlaceholders(@NotNull String input, @NotNull StatModifier modifier, @NotNull Player player, Locale locale) {
        Stat stat = modifier.getStat();
        return input.replace("{color}", stat.getColor(locale))
                .replace("{symbol}", stat.getSymbol(locale))
                .replace("{stat}", stat.getDisplayName(locale))
                .replace("{value}", NumberUtil.format1(modifier.getValue()))
                .replace("{name}", modifier.getName())
                .replace("{player}", player.getName());
    }

    public static @NotNull String applyPlaceholders(@NotNull String input, @NotNull Stat stat, @NotNull Player player, Locale locale) {
        return input.replace("{color}", stat.getColor(locale))
                .replace("{symbol}", stat.getSymbol(locale))
                .replace("{stat}", stat.getDisplayName(locale))
                .replace("{player}", player.getName());
    }

    public static @NotNull String applyPlaceholders(@NotNull String input, @NotNull Stat stat, double value, Locale locale) {
        return input.replace("{color}", stat.getColor(locale))
                .replace("{symbol}", stat.getSymbol(locale))
                .replace("{stat}", stat.getDisplayName(locale))
                .replace("{value}", NumberUtil.format1(value));
    }

    public static @NotNull String applyPlaceholders(@NotNull String input, @NotNull Stat stat, Locale locale) {
        return input.replace("{color}", stat.getColor(locale))
                .replace("{symbol}", stat.getSymbol(locale))
                .replace("{stat}", stat.getDisplayName(locale));
    }

    public static @NotNull String applyPlaceholders(@NotNull String input, @NotNull String name, @NotNull Player player) {
        return input.replace("{name}", name)
                .replace("{player}", player.getName());
    }

    public static @NotNull String applyPlaceholders(@NotNull String input, @NotNull Player player) {
        return input.replace("{player}", player.getName());
    }

}
