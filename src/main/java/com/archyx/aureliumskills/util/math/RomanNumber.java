package com.archyx.aureliumskills.util.math;

import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

public class RomanNumber {
	
	private final static TreeMap<Integer, String> map = new TreeMap<>();

    static {
        map.put(1000000000, "Ⓜ");
        map.put(900000000, "ⒸⓂ");
        map.put(500000000, "Ⓓ");
        map.put(100000000, "Ⓒ");
        map.put(90000000, "ⓍⒸ");
        map.put(50000000, "Ⓛ");
        map.put(10000000, "Ⓧ");
        map.put(9000000, "mⓍ");
        map.put(5000000, "Ⓥ");
        map.put(1000000, "m");
        map.put(900000, "cm");
        map.put(500000, "d");
        map.put(100000, "c");
        map.put(90000, "xc");
        map.put(50000, "l");
        map.put(10000, "x");
        map.put(9000, "Mx");
        map.put(5000, "v");
        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");
    }

    public static @NotNull String toRoman(int number) {
    	if (number > 0) {
	    	if (OptionL.getBoolean(Option.ENABLE_ROMAN_NUMERALS)) {
		        int l =  map.floorKey(number);
		        if ( number == l ) {
		            @Nullable String roman = map.get(number);
		            assert (null != roman);
		            return roman;
		        }
		        return map.get(l) + toRoman(number-l);
	    	}
	    	else {
	    		return String.valueOf(number);
	    	}
    	}
    	else {
    		return String.valueOf(number);
    	}
    }
}
