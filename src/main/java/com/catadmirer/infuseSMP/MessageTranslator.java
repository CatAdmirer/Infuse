package com.catadmirer.infuseSMP;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NullMarked
public class MessageTranslator {
    public static final Set<String> SUPPORTED_LOCALES = Set.of("en_US", "es");

    private final Infuse plugin = Infuse.getInstance();
    public static final Pattern CONFIG_PATTERN = Pattern.compile("<config:([a-zA-Z0-9_.-]+)>");

    @Nullable
    public String translate(String key) {
        // Getting the locale from the config
        String locale = plugin.getMainConfig().lang();

        // Defaulting to the en_US locale
        if (!SUPPORTED_LOCALES.contains(locale)) {
            Infuse.LOGGER.warn("Locale \"{}\" not recognized.  Falling back to en_US.", locale);
            locale = "en_US";
        }

        // Getting the translation
        FileConfiguration conf = getLocale(locale);

        // Getting the sentence from the config
        final String result = conf.isString(key.toLowerCase()) ? conf.getString(key.toLowerCase()) : String.join("\n", conf.getStringList(key.toLowerCase()));
        if (result == null) return null;

        final Matcher matcher = CONFIG_PATTERN.matcher(result);
        final StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            final String value = plugin.getMainConfig().config.get(matcher.group(1)).toString();
            if (value == null || value.isEmpty()) continue;

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(value));
        }

        return matcher.appendTail(buffer).toString();
    }

    public void loadAll() {
        SUPPORTED_LOCALES.forEach(this::loadLocale);
    }

    public void loadLocale(String locale) {
        plugin.saveResource("lang/base/" + locale + ".yml", true);
    }

    public FileConfiguration getLocale(String locale) {
        File baseLocaleFile = new File(plugin.getDataFolder(), "lang/base/" + locale + ".yml");
        File customLocaleFile = new File(plugin.getDataFolder(), "lang/" + locale + ".yml");

        // Loading base translations
        FileConfiguration translations = YamlConfiguration.loadConfiguration(baseLocaleFile);

        // Loading custom translations
        if (!customLocaleFile.exists()) return translations;

        FileConfiguration custom = YamlConfiguration.loadConfiguration(customLocaleFile);

        for (String key : custom.getKeys(true)) {
            translations.set(key, custom.get(key));
        }

        return translations;
    }
}
