package com.archyx.aureliumskills.util.text;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TextUtil {

    public static @NotNull String replace(@NotNull String source, @NotNull String os, @NotNull String ns) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(os);
        Objects.requireNonNull(ns);
        int i = 0;
        if ((i = source.indexOf(os, i)) >= 0) {
            char[] sourceArray = source.toCharArray();
            char[] nsArray = ns.toCharArray();
            int oLength = os.length();
            StringBuilder buf = new StringBuilder (sourceArray.length);
            buf.append (sourceArray, 0, i).append(nsArray);
            i += oLength;
            int j = i;
            // Replace all remaining instances of oldString with newString.
            while ((i = source.indexOf(os, i)) > 0) {
                buf.append (sourceArray, j, i - j).append(nsArray);
                i += oLength;
                j = i;
            }
            buf.append (sourceArray, j, sourceArray.length - j);
            source = buf.toString();
            buf.setLength(0);
        }
        return source;
    }

    public static @NotNull String replace(@NotNull String source, @NotNull String os1, @NotNull String ns1, @NotNull String os2, @NotNull String ns2) {
        return replace(replace(source, os1, ns1), os2, ns2);
    }

    public static @NotNull String replace(@NotNull String source, @NotNull String os1, @NotNull String ns1, @NotNull String os2, @NotNull String ns2, @NotNull String os3, @NotNull String ns3) {
        return replace(replace(replace(source, os1, ns1), os2, ns2), os3, ns3);
    }

    public static @NotNull String replace(@NotNull String source, @NotNull String os1, @NotNull String ns1, @NotNull String os2, @NotNull String ns2, @NotNull String os3, @NotNull String ns3, @NotNull String os4, @NotNull String ns4) {
        return replace(replace(replace(replace(source, os1, ns1), os2, ns2), os3, ns3), os4, ns4);
    }

    public static @NotNull String replace(@NotNull String source, @NotNull String os1, @NotNull String ns1, @NotNull String os2, @NotNull String ns2, @NotNull String os3, @NotNull String ns3, @NotNull String os4, @NotNull String ns4, @NotNull String os5, @NotNull String ns5) {
        return replace(replace(replace(replace(replace(source, os1, ns1), os2, ns2), os3, ns3), os4, ns4), os5, ns5);
    }

    public static @NotNull String replace(@NotNull String source, @NotNull String os1, @NotNull String ns1, @NotNull String os2, @NotNull String ns2, @NotNull String os3, @NotNull String ns3, @NotNull String os4, @NotNull String ns4, @NotNull String os5, @NotNull String ns5, @NotNull String os6, @NotNull String ns6) {
        return replace(replace(replace(replace(replace(replace(source, os1, ns1), os2, ns2), os3, ns3), os4, ns4), os5, ns5), os6, ns6);
    }

    public static @NotNull String replaceNonEscaped(@NotNull String source, @NotNull String os, @NotNull String ns) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(os);
        Objects.requireNonNull(ns);
        String replaced = replace(source, "\\" + os, "\uE000"); // Replace escaped characters with intermediate char
        replaced = replace(replaced, os, ns); // Replace normal chars
        return replace(replaced, "\uE000", os); // Replace intermediate with original
    }

    public static @NotNull String removeEnd(@NotNull String str, @NotNull String remove) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(remove);
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }

    public static boolean isEmpty(@Nullable CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static @NotNull String capitalize(@NotNull String str) {
        Objects.requireNonNull(str);
        int strLen = length(str);
        if (strLen == 0) {
            return str;
        }
        
        int firstCodepoint = str.codePointAt(0);
        int newCodePoint = Character.toTitleCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            // already capitalized
            return str;
        }

        int[] newCodePoints = new int[strLen]; // cannot be longer than the char array
        int outOffset = 0;
        newCodePoints[outOffset++] = newCodePoint; // copy the first codepoint
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
            int codepoint = str.codePointAt(inOffset);
            newCodePoints[outOffset++] = codepoint; // copy the remaining ones
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    public static int length(@Nullable CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    public static @NotNull String repeat(char ch, int repeat) {
        if (repeat <= 0) {
            return "";
        }
        char[] buf = new char[repeat];
        Arrays.fill(buf, ch);
        return new String(buf);
    }


    public static @NotNull String repeat(@NotNull String str, int repeat) {
        Objects.requireNonNull(str);
        if (repeat <= 0) {
            return "";
        }
        int inputLength = str.length();
        if (repeat == 1 || inputLength == 0) {
            return str;
        }
        if (inputLength == 1 && repeat <= 8192) {
            return repeat(str.charAt(0), repeat);
        }

        int outputLength = inputLength * repeat;
        switch (inputLength) {
            case 1 :
                return repeat(str.charAt(0), repeat);
            case 2 :
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                char[] output2 = new char[outputLength];
                for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
                    output2[i] = ch0;
                    output2[i + 1] = ch1;
                }
                return new String(output2);
            default :
                StringBuilder buf = new StringBuilder(outputLength);
                for (int i = 0; i < repeat; i++) {
                    buf.append(str);
                }
                return buf.toString();
        }
    }

    private static @NotNull Set<Integer> generateDelimiterSet(char @NotNull [] delimiters) {
        Objects.requireNonNull(delimiters);
        Set<Integer> delimiterHashSet = new HashSet<>();
        if (delimiters.length == 0) {
            return delimiterHashSet;
        }

        for (int index = 0; index < delimiters.length; index++) {
            delimiterHashSet.add(Character.codePointAt(delimiters, index));
        }
        return delimiterHashSet;
    }

    public static @NotNull String capitalizeWord(@NotNull String str, char @NotNull ... delimiters) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(delimiters);
        if (isEmpty(str)) {
            return str;
        }
        Set<Integer> delimiterSet = generateDelimiterSet(delimiters);
        int strLen = str.length();
        int[] newCodePoints = new int[strLen];
        int outOffset = 0;

        boolean capitalizeNext = true;
        for (int index = 0; index < strLen;) {
            int codePoint = str.codePointAt(index);

            if (delimiterSet.contains(codePoint)) {
                capitalizeNext = true;
                newCodePoints[outOffset++] = codePoint;
                index += Character.charCount(codePoint);
            } else if (capitalizeNext) {
                int titleCaseCodePoint = Character.toTitleCase(codePoint);
                newCodePoints[outOffset++] = titleCaseCodePoint;
                index += Character.charCount(titleCaseCodePoint);
                capitalizeNext = false;
            } else {
                newCodePoints[outOffset++] = codePoint;
                index += Character.charCount(codePoint);
            }
        }
        return new String(newCodePoints, 0, outOffset);
    }

    public static @NotNull String capitalizeWord(@NotNull String str) {
        Objects.requireNonNull(str);
        return capitalizeWord(str, ' ');
    }


}
