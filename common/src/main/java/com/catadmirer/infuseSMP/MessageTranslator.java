package com.catadmirer.infuseSMP;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.util.*;

@NullMarked
public class MessageTranslator {
    private final Infuse plugin;
    private final File langFolder;
    private final File baseFolder;

    public MessageTranslator() {
        this.plugin = Infuse.getInstance();
        this.langFolder = new File(plugin.infuseDir(), "lang");
        this.baseFolder = new File(plugin.infuseDir(), "lang/base");
    }

    @Nullable
    public String translate(String key) {
        // Getting the locale from the config
        String locale = plugin.getLocale();

        // Defaulting to the en_US locale
        if (!new File(baseFolder, (locale + ".yml")).exists()) {
            Infuse.LOGGER.warn("Locale \"{}\" not recognized.  Falling back to en_US.", locale);
            locale = "en_US";
        }

        // Getting the translation
        return getLocale(locale).get(key.toLowerCase());
    }

    public void loadAll() {
        try {
            Enumeration<URL> langs = this.getClass().getClassLoader().getResources("lang/base");
            while (langs.hasMoreElements()) loadLocale(langs.nextElement());
        } catch (IOException e) {
            Infuse.LOGGER.error("Ran into IO error while parsing lang sources.", e);
        }
    }

    public void loadLocale(String locale) {
        URL url = this.getClass().getClassLoader().getResource("lang/base/" + locale + ".yml");

        if (url == null) {
            Infuse.LOGGER.warn("Invalid locale '{}'.  Make sure it's a supported language.", locale);
            return;
        }

        loadLocale(url);
    }

    public void loadLocale(URL localeUrl) {
        try {
            InputStream resource = localeUrl.openStream();
            assert resource != null;

            File fileRef = new File(baseFolder, localeUrl.getFile());
            fileRef.getParentFile().mkdirs();
            fileRef.createNewFile();

            FileOutputStream file = new FileOutputStream(fileRef);
            resource.transferTo(file);
            file.close();
            resource.close();
        } catch (AssertionError e) {
            Infuse.LOGGER.error("Invalid locale '{}'.  Make sure it's a supported language.", localeUrl.getFile(), e);
        } catch (FileNotFoundException e) {
            Infuse.LOGGER.error("Could not find the file I just created...", e);
        } catch (IOException e) {
            Infuse.LOGGER.error("Ran into an IOException while parsing {}.", localeUrl.getFile(), e);
        }
    }

    public Map<String,String> getLocale(String locale) {
        File baseLocaleFile = new File(baseFolder, locale + ".yml");
        File customLocaleFile = new File(langFolder, locale + ".yml");

        // Loading base translations
        Map<String,String> translations = new HashMap<>();

        Yaml yml = new Yaml();

        try {
            Map<String,Object> baseTranslations = yml.load(new FileReader(baseLocaleFile));
            baseTranslations.forEach((k,v) -> {
                if (v instanceof String s) translations.put(k, s);

                if (v instanceof List<?> l) {
                    translations.put(k, String.join("\n", l.toArray(String[]::new)));
                }
            });
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Loading custom translations
        if (!customLocaleFile.exists()) return translations;

        try {
            Map<String,Object> customTranslations = yml.load(new FileReader(customLocaleFile));
            customTranslations.forEach((k,v) -> {
                if (v instanceof String s) translations.put(k, s);

                if (v instanceof List<?> l) {
                    translations.put(k, String.join("\n", l.toArray(String[]::new)));
                }
            });
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return translations;
    }
}
