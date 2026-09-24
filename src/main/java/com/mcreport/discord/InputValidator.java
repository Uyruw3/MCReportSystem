package com.mcreport.discord;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_.*]{1,17}");
    private static final Pattern URL = Pattern.compile("https?://[^\\s<>]{4,500}", Pattern.CASE_INSENSITIVE);

    private InputValidator() {
    }

    public static boolean isValidPlayerName(String value) {
        return value != null && PLAYER_NAME.matcher(value.trim()).matches();
    }

    public static String normalizeText(String value, int maxLength) {
        if (value == null) return "";
        String normalized = value.trim().replace("\u0000", "");
        return normalized.length() > maxLength ? normalized.substring(0, maxLength) : normalized;
    }

    public static boolean isConfiguredReason(String value, List<String> reasons) {
        if (value == null) return false;
        String normalized = value.trim();
        return reasons.stream().anyMatch(reason -> reason.equalsIgnoreCase(normalized));
    }

    public static String normalizeEvidence(String value) {
        if (value == null || value.isBlank()) return "";
        StringBuilder result = new StringBuilder();
        for (String candidate : value.trim().split("\\s+")) {
            if (candidate.length() > 500 || !URL.matcher(candidate).matches()) continue;
            try {
                URI uri = URI.create(candidate);
                String scheme = uri.getScheme();
                if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                    continue;
                }
                if (result.length() > 0) result.append('\n');
                result.append(candidate);
            } catch (IllegalArgumentException ignored) {
                // Los valores no URL se descartan para no presentar evidencia engañosa.
            }
            if (result.length() >= 900) break;
        }
        return result.length() > 900 ? result.substring(0, 900) : result.toString();
    }

    public static boolean isSafeDuration(String value, int minimum, int maximum) {
        try {
            int parsed = Integer.parseInt(value == null ? "" : value.trim());
            return parsed >= minimum && parsed <= maximum;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
