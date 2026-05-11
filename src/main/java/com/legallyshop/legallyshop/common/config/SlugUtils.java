package com.legallyshop.legallyshop.common.config;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {

    private static final Pattern NON_LATIN     = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE    = Pattern.compile("[\\s]+");
    private static final Pattern MULTI_DASHES  = Pattern.compile("-{2,}");

    public static String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return NON_LATIN.matcher(
                        WHITESPACE.matcher(normalized)
                                .replaceAll("-"))
                .replaceAll("")
                .toLowerCase(Locale.ROOT)
                .replaceAll(MULTI_DASHES.pattern(), "-")
                .replaceAll("^-|-$", "");
    }
}