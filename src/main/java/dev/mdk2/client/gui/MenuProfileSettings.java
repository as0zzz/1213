package dev.mdk2.client.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class MenuProfileSettings {
    public int languageIndex;
    public int menuScaleIndex;
    public int safeModeIndex;
    public String style;

    public static MenuProfileSettings defaults() {
        final MenuProfileSettings settings = new MenuProfileSettings();
        settings.languageIndex = 0;
        settings.menuScaleIndex = 4;
        settings.safeModeIndex = 0;
        settings.style = "Dark";
        return settings;
    }

    public MenuProfileSettings copy() {
        final MenuProfileSettings copy = new MenuProfileSettings();
        copy.languageIndex = this.languageIndex;
        copy.menuScaleIndex = this.menuScaleIndex;
        copy.safeModeIndex = this.safeModeIndex;
        copy.style = this.style;
        return copy;
    }

    public static MenuProfileSettings load(final Path path) throws IOException {
        final MenuProfileSettings settings = defaults();
        if (!Files.exists(path)) {
            return settings;
        }
        final Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        }
        settings.languageIndex = parseInt(properties.getProperty("languageIndex"), settings.languageIndex);
        settings.menuScaleIndex = parseInt(properties.getProperty("menuScaleIndex"), settings.menuScaleIndex);
        settings.safeModeIndex = parseInt(properties.getProperty("safeModeIndex"), settings.safeModeIndex);
        settings.style = properties.getProperty("style", settings.style);
        return settings;
    }

    public void save(final Path path) throws IOException {
        final Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        final Properties properties = new Properties();
        properties.setProperty("languageIndex", Integer.toString(this.languageIndex));
        properties.setProperty("menuScaleIndex", Integer.toString(this.menuScaleIndex));
        properties.setProperty("safeModeIndex", Integer.toString(this.safeModeIndex));
        properties.setProperty("style", this.style);
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            properties.store(outputStream, "MDK2 menu profile");
        }
    }

    private static int parseInt(final String value, final int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException ignored) {
            return fallback;
        }
    }
}
