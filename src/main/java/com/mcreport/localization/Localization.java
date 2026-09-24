package com.mcreport.localization;

import com.mcreport.MCReportPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Loads bundled translations while keeping the Spanish configuration messages
 * compatible with existing installations.
 */
public final class Localization {

    private static final String DEFAULT_LANGUAGE = "es";
    private static final String[] SUPPORTED_LANGUAGES = {"es", "en", "pt", "fr"};

    private final MCReportPlugin plugin;
    private YamlConfiguration messages;
    private String language;

    public Localization(MCReportPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        String configured = plugin.getConfig().getString("language", DEFAULT_LANGUAGE);
        String normalized = configured == null ? DEFAULT_LANGUAGE : configured.trim().toLowerCase(Locale.ROOT);
        language = isSupported(normalized) ? normalized : DEFAULT_LANGUAGE;
        if (!language.equals(normalized)) {
            plugin.getLogger().warning("Idioma no compatible '" + configured
                    + "'; se usará español (es). Idiomas disponibles: en, es, pt, fr.");
        }
        messages = load(language);
    }

    public String getLanguage() {
        return language;
    }

    public String message(String key, Object... replacements) {
        String value = messages.getString(key);
        if (DEFAULT_LANGUAGE.equals(language)) {
            String configured = plugin.getConfig().getString(key);
            if (configured != null && !configured.isBlank()) {
                value = configured;
            }
        }
        if (value == null && !DEFAULT_LANGUAGE.equals(language)) {
            value = load(DEFAULT_LANGUAGE).getString(key);
        }
        if (value == null) {
            plugin.getLogger().warning("Falta el mensaje de idioma: " + key);
            return key;
        }
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            value = value.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
        }
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    private YamlConfiguration load(String locale) {
        String resource = "messages_" + locale + ".yml";
        try (InputStream stream = plugin.getResource(resource)) {
            if (stream == null) {
                plugin.getLogger().warning("No se encontró el recurso de idioma " + resource
                        + "; se usará español.");
                if (!DEFAULT_LANGUAGE.equals(locale)) {
                    return load(DEFAULT_LANGUAGE);
                }
                return new YamlConfiguration();
            }
            return YamlConfiguration.loadConfiguration(
                    new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (Exception exception) {
            plugin.getLogger().warning("No se pudo cargar " + resource + ": " + exception.getMessage());
            if (!DEFAULT_LANGUAGE.equals(locale)) {
                return load(DEFAULT_LANGUAGE);
            }
            return new YamlConfiguration();
        }
    }

    private boolean isSupported(String value) {
        for (String supported : SUPPORTED_LANGUAGES) {
            if (supported.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
