package dev.mdk2.client.config;

import dev.mdk2.client.gui.MenuProfileSettings;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BindSetting;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ColorSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.settings.Setting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public final class ConfigManager {
    private static final String EXTENSION = ".properties";
    private final Path configDirectory;
    private String selectedConfigName = "Unnamed";

    public ConfigManager(final Path configDirectory) {
        this.configDirectory = configDirectory;
    }

    public String getSelectedConfigName() {
        return this.selectedConfigName;
    }

    public void setSelectedConfigName(final String selectedConfigName) {
        this.selectedConfigName = sanitizeName(selectedConfigName);
    }

    public List<String> listConfigNames() throws IOException {
        if (!Files.exists(this.configDirectory)) {
            return Collections.emptyList();
        }
        final List<String> names = new ArrayList<String>();
        try (Stream<Path> stream = Files.list(this.configDirectory)) {
            stream.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(EXTENSION))
                .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                .forEach(path -> names.add(stripExtension(path.getFileName().toString())));
        }
        return names;
    }

    public String createNextName() throws IOException {
        final List<String> names = listConfigNames();
        int index = 2;
        String candidate = "Config " + index;
        while (names.contains(candidate)) {
            index++;
            candidate = "Config " + index;
        }
        return candidate;
    }

    public void ensureDefaultConfig(final Iterable<Module> modules) throws IOException {
        final List<String> names = listConfigNames();
        if (names.isEmpty()) {
            save(this.selectedConfigName, modules);
            return;
        }
        if (!names.contains(this.selectedConfigName)) {
            this.selectedConfigName = names.get(0);
        }
        load(this.selectedConfigName, modules);
    }

    public void saveSelected(final Iterable<Module> modules) throws IOException {
        save(this.selectedConfigName, modules);
    }

    public void saveSelected(final Iterable<Module> modules, final MenuProfileSettings profileSettings) throws IOException {
        save(this.selectedConfigName, modules, profileSettings);
    }

    public void loadSelected(final Iterable<Module> modules) throws IOException {
        load(this.selectedConfigName, modules);
    }

    public MenuProfileSettings loadSelectedProfile(final MenuProfileSettings defaults) throws IOException {
        return loadProfile(this.selectedConfigName, defaults);
    }

    public void delete(final String name) throws IOException {
        final String sanitized = sanitizeName(name);
        Files.deleteIfExists(configPath(sanitized));
        final List<String> names = listConfigNames();
        this.selectedConfigName = names.isEmpty() ? "Unnamed" : names.get(0);
    }

    public void save(final String name, final Iterable<Module> modules) throws IOException {
        save(name, modules, null);
    }

    public void save(final String name, final Iterable<Module> modules, final MenuProfileSettings profileSettings) throws IOException {
        final String sanitized = sanitizeName(name);
        this.selectedConfigName = sanitized;
        Files.createDirectories(this.configDirectory);
        final Properties properties = new Properties();
        properties.setProperty("config.name", sanitized);
        for (final Module module : modules) {
            final String moduleKey = "module." + module.getClass().getName();
            properties.setProperty(moduleKey + ".enabled", Boolean.toString(module.isEnabled()));
            for (final Setting<?> setting : module.getSettings()) {
                final String settingKey = moduleKey + ".setting." + setting.getName();
                properties.setProperty(settingKey, serializeSetting(setting));
            }
        }
        if (profileSettings != null) {
            properties.setProperty("ui.languageIndex", Integer.toString(profileSettings.languageIndex));
            properties.setProperty("ui.menuScaleIndex", Integer.toString(profileSettings.menuScaleIndex));
            properties.setProperty("ui.safeModeIndex", Integer.toString(profileSettings.safeModeIndex));
            properties.setProperty("ui.style", profileSettings.style);
        }
        try (OutputStream outputStream = Files.newOutputStream(configPath(sanitized))) {
            properties.store(outputStream, "MDK2 config");
        }
    }

    public void load(final String name, final Iterable<Module> modules) throws IOException {
        final String sanitized = sanitizeName(name);
        final Path path = configPath(sanitized);
        if (!Files.exists(path)) {
            return;
        }
        this.selectedConfigName = sanitized;
        final Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        }
        for (final Module module : modules) {
            final String moduleKey = "module." + module.getClass().getName();
            final String enabledValue = properties.getProperty(moduleKey + ".enabled");
            if (enabledValue != null && module.isToggleable()) {
                module.setEnabled(Boolean.parseBoolean(enabledValue));
            }
            for (final Setting<?> setting : module.getSettings()) {
                final String rawValue = properties.getProperty(moduleKey + ".setting." + setting.getName());
                if (rawValue != null) {
                    applySettingValue(setting, rawValue);
                }
            }
        }
    }

    public MenuProfileSettings loadProfile(final String name, final MenuProfileSettings defaults) throws IOException {
        final MenuProfileSettings profileSettings = defaults == null ? MenuProfileSettings.defaults() : defaults.copy();
        final String sanitized = sanitizeName(name);
        final Path path = configPath(sanitized);
        if (!Files.exists(path)) {
            return profileSettings;
        }
        final Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        }
        profileSettings.languageIndex = parseInt(properties.getProperty("ui.languageIndex"), profileSettings.languageIndex);
        profileSettings.menuScaleIndex = parseInt(properties.getProperty("ui.menuScaleIndex"), profileSettings.menuScaleIndex);
        profileSettings.safeModeIndex = parseInt(properties.getProperty("ui.safeModeIndex"), profileSettings.safeModeIndex);
        profileSettings.style = properties.getProperty("ui.style", profileSettings.style);
        return profileSettings;
    }

    public static String sanitizeName(final String raw) {
        if (raw == null) {
            return "Unnamed";
        }
        final String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return "Unnamed";
        }
        final String sanitized = trimmed.replaceAll("[\\\\/:*?\"<>|]+", "_");
        return sanitized.isEmpty() ? "Unnamed" : sanitized;
    }

    private Path configPath(final String name) {
        return this.configDirectory.resolve(name + EXTENSION);
    }

    private static String stripExtension(final String fileName) {
        return fileName.endsWith(EXTENSION) ? fileName.substring(0, fileName.length() - EXTENSION.length()) : fileName;
    }

    private static String serializeSetting(final Setting<?> setting) {
        if (setting instanceof BooleanSetting) {
            return Boolean.toString(((BooleanSetting) setting).getValue().booleanValue());
        }
        if (setting instanceof ModeSetting) {
            return ((ModeSetting) setting).getValue();
        }
        if (setting instanceof NumberSetting) {
            return Double.toString(((NumberSetting) setting).getValue().doubleValue());
        }
        if (setting instanceof ColorSetting) {
            return Integer.toString(((ColorSetting) setting).getColor());
        }
        if (setting instanceof BindSetting) {
            return Integer.toString(((BindSetting) setting).getKey());
        }
        return String.valueOf(setting.getValue());
    }

    private static void applySettingValue(final Setting<?> setting, final String rawValue) {
        if (setting instanceof BooleanSetting) {
            ((BooleanSetting) setting).setValue(Boolean.parseBoolean(rawValue));
            return;
        }
        if (setting instanceof ModeSetting) {
            final ModeSetting modeSetting = (ModeSetting) setting;
            if (modeSetting.getModes().contains(rawValue)) {
                while (!modeSetting.getValue().equals(rawValue)) {
                    modeSetting.next();
                }
            }
            return;
        }
        if (setting instanceof NumberSetting) {
            try {
                ((NumberSetting) setting).setValue(Double.parseDouble(rawValue));
            } catch (final NumberFormatException ignored) {
            }
            return;
        }
        if (setting instanceof ColorSetting) {
            try {
                ((ColorSetting) setting).setColor(Integer.parseInt(rawValue));
            } catch (final NumberFormatException ignored) {
            }
            return;
        }
        if (setting instanceof BindSetting) {
            try {
                ((BindSetting) setting).setKey(Integer.parseInt(rawValue));
            } catch (final NumberFormatException ignored) {
            }
        }
    }

    private static int parseInt(final String rawValue, final int fallback) {
        if (rawValue == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(rawValue);
        } catch (final NumberFormatException ignored) {
            return fallback;
        }
    }
}
