package com.archyx.aureliumskills.util.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NumberUtil {

    private static final NumberFormat zero = new DecimalFormat("#");
    private static final NumberFormat one = new DecimalFormat("#.#");
    private static final NumberFormat two = new DecimalFormat("#.##");

    static {
        zero.setRoundingMode(RoundingMode.HALF_UP);
        one.setRoundingMode(RoundingMode.HALF_UP);
        two.setRoundingMode(RoundingMode.HALF_UP);
    }

    public static @NotNull String format0(double input) {
        return zero.format(input);
    }

    public static @NotNull String format1(double input) {
        return one.format(input);
    }

    public static @NotNull String format2(double input) {
        return two.format(input);
    }

    public static int toInt(String str) {
        return toInt(str, 0);
    }

    public static int toInt(@Nullable String str, int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

}
