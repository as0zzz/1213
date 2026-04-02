package dev.mdk2.client.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class MenuDebugProfile {
    public double designWidth;
    public double designHeight;
    public double displayScaleCap;
    public double windowRadius;

    public double fontSize;
    public double textScale;
    public double valueScale;
    public double miniScale;

    public double sidebarWidth;
    public double sidebarXPadding;
    public double sidebarTopPadding;
    public double sidebarEntrySpacing;
    public double sidebarSubEntrySpacing;
    public double sidebarFooterInset;
    public double contentXOffset;
    public double contentYOffset;
    public double contentRightPadding;
    public double columnGap;

    public double topbarYOffset;
    public double topbarButtonSize;
    public double topbarSelectorHeight;
    public double topbarPresetWidth;
    public double topbarModeWidth;
    public double topbarGap;

    public double panelHeaderOffset;
    public double panelRowStep;
    public double panelStackGap;
    public double panelRadius;

    public double logoTitleScale;
    public double logoSubtitleScale;
    public double logoTextXOffset;
    public double logoTitleYOffset;
    public double logoSubtitleYOffset;

    public double footerNameScale;
    public double footerStatusScale;
    public double footerHeight;
    public double footerBottomInset;
    public double footerTextXOffset;
    public double footerAvatarRadius;

    public double dropdownWidth;
    public double sliderTrackWidth;
    public double menuOffsetX;
    public double menuOffsetY;
    public double debugOverlayX;
    public double debugOverlayY;

    public static MenuDebugProfile defaults() {
        final MenuDebugProfile profile = new MenuDebugProfile();
        profile.reset();
        return profile;
    }

    public void reset() {
        this.designWidth = 900.0D;
        this.designHeight = 600.0D;
        this.displayScaleCap = 0.50D;
        this.windowRadius = 30.0D;

        this.fontSize = 34.0D;
        this.textScale = 0.36D;
        this.valueScale = 0.30D;
        this.miniScale = 0.30D;

        this.sidebarWidth = 200.0D;
        this.sidebarXPadding = 25.0D;
        this.sidebarTopPadding = 25.0D;
        this.sidebarEntrySpacing = 40.0D;
        this.sidebarSubEntrySpacing = 30.0D;
        this.sidebarFooterInset = 60.0D;
        this.contentXOffset = 20.0D;
        this.contentYOffset = 70.0D;
        this.contentRightPadding = 20.0D;
        this.columnGap = 20.0D;

        this.topbarYOffset = 25.0D;
        this.topbarButtonSize = 30.0D;
        this.topbarSelectorHeight = 30.0D;
        this.topbarPresetWidth = 140.0D;
        this.topbarModeWidth = 120.0D;
        this.topbarGap = 20.0D;

        this.panelHeaderOffset = 35.0D;
        this.panelRowStep = 31.0D;
        this.panelStackGap = 20.0D;
        this.panelRadius = 15.0D;

        this.logoTitleScale = 0.70D;
        this.logoSubtitleScale = 0.34D;
        this.logoTextXOffset = 34.0D;
        this.logoTitleYOffset = -4.0D;
        this.logoSubtitleYOffset = 26.0D;

        this.footerNameScale = 0.30D;
        this.footerStatusScale = 0.28D;
        this.footerHeight = 50.0D;
        this.footerBottomInset = 62.0D;
        this.footerTextXOffset = 40.0D;
        this.footerAvatarRadius = 15.0D;

        this.dropdownWidth = 90.0D;
        this.sliderTrackWidth = 75.0D;
        this.menuOffsetX = 0.0D;
        this.menuOffsetY = 0.0D;
        this.debugOverlayX = Double.NaN;
        this.debugOverlayY = Double.NaN;
    }

    public void copyFrom(final MenuDebugProfile other) {
        this.designWidth = other.designWidth;
        this.designHeight = other.designHeight;
        this.displayScaleCap = other.displayScaleCap;
        this.windowRadius = other.windowRadius;
        this.fontSize = other.fontSize;
        this.textScale = other.textScale;
        this.valueScale = other.valueScale;
        this.miniScale = other.miniScale;
        this.sidebarWidth = other.sidebarWidth;
        this.sidebarXPadding = other.sidebarXPadding;
        this.sidebarTopPadding = other.sidebarTopPadding;
        this.sidebarEntrySpacing = other.sidebarEntrySpacing;
        this.sidebarSubEntrySpacing = other.sidebarSubEntrySpacing;
        this.sidebarFooterInset = other.sidebarFooterInset;
        this.contentXOffset = other.contentXOffset;
        this.contentYOffset = other.contentYOffset;
        this.contentRightPadding = other.contentRightPadding;
        this.columnGap = other.columnGap;
        this.topbarYOffset = other.topbarYOffset;
        this.topbarButtonSize = other.topbarButtonSize;
        this.topbarSelectorHeight = other.topbarSelectorHeight;
        this.topbarPresetWidth = other.topbarPresetWidth;
        this.topbarModeWidth = other.topbarModeWidth;
        this.topbarGap = other.topbarGap;
        this.panelHeaderOffset = other.panelHeaderOffset;
        this.panelRowStep = other.panelRowStep;
        this.panelStackGap = other.panelStackGap;
        this.panelRadius = other.panelRadius;
        this.logoTitleScale = other.logoTitleScale;
        this.logoSubtitleScale = other.logoSubtitleScale;
        this.logoTextXOffset = other.logoTextXOffset;
        this.logoTitleYOffset = other.logoTitleYOffset;
        this.logoSubtitleYOffset = other.logoSubtitleYOffset;
        this.footerNameScale = other.footerNameScale;
        this.footerStatusScale = other.footerStatusScale;
        this.footerHeight = other.footerHeight;
        this.footerBottomInset = other.footerBottomInset;
        this.footerTextXOffset = other.footerTextXOffset;
        this.footerAvatarRadius = other.footerAvatarRadius;
        this.dropdownWidth = other.dropdownWidth;
        this.sliderTrackWidth = other.sliderTrackWidth;
        this.menuOffsetX = other.menuOffsetX;
        this.menuOffsetY = other.menuOffsetY;
        this.debugOverlayX = other.debugOverlayX;
        this.debugOverlayY = other.debugOverlayY;
    }

    public void save(final Path path) throws IOException {
        final Properties properties = this.toProperties();
        final Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            properties.store(outputStream, "MDK2 menu debug profile");
        }
    }

    public static MenuDebugProfile load(final Path path) throws IOException {
        final MenuDebugProfile profile = defaults();
        if (!Files.exists(path)) {
            return profile;
        }
        final Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        }
        profile.designWidth = read(properties, "designWidth", profile.designWidth);
        profile.designHeight = read(properties, "designHeight", profile.designHeight);
        profile.displayScaleCap = read(properties, "displayScaleCap", profile.displayScaleCap);
        profile.windowRadius = read(properties, "windowRadius", profile.windowRadius);

        profile.fontSize = read(properties, "fontSize", profile.fontSize);
        profile.textScale = read(properties, "textScale", profile.textScale);
        profile.valueScale = read(properties, "valueScale", profile.valueScale);
        profile.miniScale = read(properties, "miniScale", profile.miniScale);

        profile.sidebarWidth = read(properties, "sidebarWidth", profile.sidebarWidth);
        profile.sidebarXPadding = read(properties, "sidebarXPadding", profile.sidebarXPadding);
        profile.sidebarTopPadding = read(properties, "sidebarTopPadding", profile.sidebarTopPadding);
        profile.sidebarEntrySpacing = read(properties, "sidebarEntrySpacing", profile.sidebarEntrySpacing);
        profile.sidebarSubEntrySpacing = read(properties, "sidebarSubEntrySpacing", profile.sidebarSubEntrySpacing);
        profile.sidebarFooterInset = read(properties, "sidebarFooterInset", profile.sidebarFooterInset);
        profile.contentXOffset = read(properties, "contentXOffset", profile.contentXOffset);
        profile.contentYOffset = read(properties, "contentYOffset", profile.contentYOffset);
        profile.contentRightPadding = read(properties, "contentRightPadding", profile.contentRightPadding);
        profile.columnGap = read(properties, "columnGap", profile.columnGap);

        profile.topbarYOffset = read(properties, "topbarYOffset", profile.topbarYOffset);
        profile.topbarButtonSize = read(properties, "topbarButtonSize", profile.topbarButtonSize);
        profile.topbarSelectorHeight = read(properties, "topbarSelectorHeight", profile.topbarSelectorHeight);
        profile.topbarPresetWidth = read(properties, "topbarPresetWidth", profile.topbarPresetWidth);
        profile.topbarModeWidth = read(properties, "topbarModeWidth", profile.topbarModeWidth);
        profile.topbarGap = read(properties, "topbarGap", profile.topbarGap);

        profile.panelHeaderOffset = read(properties, "panelHeaderOffset", profile.panelHeaderOffset);
        profile.panelRowStep = read(properties, "panelRowStep", profile.panelRowStep);
        profile.panelStackGap = read(properties, "panelStackGap", profile.panelStackGap);
        profile.panelRadius = read(properties, "panelRadius", profile.panelRadius);

        profile.logoTitleScale = read(properties, "logoTitleScale", profile.logoTitleScale);
        profile.logoSubtitleScale = read(properties, "logoSubtitleScale", profile.logoSubtitleScale);
        profile.logoTextXOffset = read(properties, "logoTextXOffset", profile.logoTextXOffset);
        profile.logoTitleYOffset = read(properties, "logoTitleYOffset", profile.logoTitleYOffset);
        profile.logoSubtitleYOffset = read(properties, "logoSubtitleYOffset", profile.logoSubtitleYOffset);

        profile.footerNameScale = read(properties, "footerNameScale", profile.footerNameScale);
        profile.footerStatusScale = read(properties, "footerStatusScale", profile.footerStatusScale);
        profile.footerHeight = read(properties, "footerHeight", profile.footerHeight);
        profile.footerBottomInset = read(properties, "footerBottomInset", profile.footerBottomInset);
        profile.footerTextXOffset = read(properties, "footerTextXOffset", profile.footerTextXOffset);
        profile.footerAvatarRadius = read(properties, "footerAvatarRadius", profile.footerAvatarRadius);
        profile.dropdownWidth = read(properties, "dropdownWidth", profile.dropdownWidth);
        profile.sliderTrackWidth = read(properties, "sliderTrackWidth", profile.sliderTrackWidth);
        profile.menuOffsetX = read(properties, "menuOffsetX", profile.menuOffsetX);
        profile.menuOffsetY = read(properties, "menuOffsetY", profile.menuOffsetY);
        profile.debugOverlayX = read(properties, "debugOverlayX", profile.debugOverlayX);
        profile.debugOverlayY = read(properties, "debugOverlayY", profile.debugOverlayY);
        return profile;
    }

    public String exportBlock() {
        final StringBuilder builder = new StringBuilder();
        builder.append("profile.designWidth = ").append(format(this.designWidth)).append(";\n");
        builder.append("profile.designHeight = ").append(format(this.designHeight)).append(";\n");
        builder.append("profile.displayScaleCap = ").append(format(this.displayScaleCap)).append(";\n");
        builder.append("profile.windowRadius = ").append(format(this.windowRadius)).append(";\n");
        builder.append("profile.fontSize = ").append(format(this.fontSize)).append(";\n");
        builder.append("profile.textScale = ").append(format(this.textScale)).append(";\n");
        builder.append("profile.valueScale = ").append(format(this.valueScale)).append(";\n");
        builder.append("profile.miniScale = ").append(format(this.miniScale)).append(";\n");
        builder.append("profile.sidebarWidth = ").append(format(this.sidebarWidth)).append(";\n");
        builder.append("profile.sidebarXPadding = ").append(format(this.sidebarXPadding)).append(";\n");
        builder.append("profile.sidebarTopPadding = ").append(format(this.sidebarTopPadding)).append(";\n");
        builder.append("profile.sidebarEntrySpacing = ").append(format(this.sidebarEntrySpacing)).append(";\n");
        builder.append("profile.sidebarSubEntrySpacing = ").append(format(this.sidebarSubEntrySpacing)).append(";\n");
        builder.append("profile.sidebarFooterInset = ").append(format(this.sidebarFooterInset)).append(";\n");
        builder.append("profile.contentXOffset = ").append(format(this.contentXOffset)).append(";\n");
        builder.append("profile.contentYOffset = ").append(format(this.contentYOffset)).append(";\n");
        builder.append("profile.contentRightPadding = ").append(format(this.contentRightPadding)).append(";\n");
        builder.append("profile.columnGap = ").append(format(this.columnGap)).append(";\n");
        builder.append("profile.topbarYOffset = ").append(format(this.topbarYOffset)).append(";\n");
        builder.append("profile.topbarButtonSize = ").append(format(this.topbarButtonSize)).append(";\n");
        builder.append("profile.topbarSelectorHeight = ").append(format(this.topbarSelectorHeight)).append(";\n");
        builder.append("profile.topbarPresetWidth = ").append(format(this.topbarPresetWidth)).append(";\n");
        builder.append("profile.topbarModeWidth = ").append(format(this.topbarModeWidth)).append(";\n");
        builder.append("profile.topbarGap = ").append(format(this.topbarGap)).append(";\n");
        builder.append("profile.panelHeaderOffset = ").append(format(this.panelHeaderOffset)).append(";\n");
        builder.append("profile.panelRowStep = ").append(format(this.panelRowStep)).append(";\n");
        builder.append("profile.panelStackGap = ").append(format(this.panelStackGap)).append(";\n");
        builder.append("profile.panelRadius = ").append(format(this.panelRadius)).append(";\n");
        builder.append("profile.logoTitleScale = ").append(format(this.logoTitleScale)).append(";\n");
        builder.append("profile.logoSubtitleScale = ").append(format(this.logoSubtitleScale)).append(";\n");
        builder.append("profile.logoTextXOffset = ").append(format(this.logoTextXOffset)).append(";\n");
        builder.append("profile.logoTitleYOffset = ").append(format(this.logoTitleYOffset)).append(";\n");
        builder.append("profile.logoSubtitleYOffset = ").append(format(this.logoSubtitleYOffset)).append(";\n");
        builder.append("profile.footerNameScale = ").append(format(this.footerNameScale)).append(";\n");
        builder.append("profile.footerStatusScale = ").append(format(this.footerStatusScale)).append(";\n");
        builder.append("profile.footerHeight = ").append(format(this.footerHeight)).append(";\n");
        builder.append("profile.footerBottomInset = ").append(format(this.footerBottomInset)).append(";\n");
        builder.append("profile.footerTextXOffset = ").append(format(this.footerTextXOffset)).append(";\n");
        builder.append("profile.footerAvatarRadius = ").append(format(this.footerAvatarRadius)).append(";\n");
        builder.append("profile.dropdownWidth = ").append(format(this.dropdownWidth)).append(";\n");
        builder.append("profile.sliderTrackWidth = ").append(format(this.sliderTrackWidth)).append(";\n");
        builder.append("profile.menuOffsetX = ").append(format(this.menuOffsetX)).append(";\n");
        builder.append("profile.menuOffsetY = ").append(format(this.menuOffsetY)).append(";\n");
        builder.append("profile.debugOverlayX = ").append(format(this.debugOverlayX)).append(";\n");
        builder.append("profile.debugOverlayY = ").append(format(this.debugOverlayY)).append(";\n");
        return builder.toString();
    }

    private Properties toProperties() {
        final Properties properties = new Properties();
        properties.setProperty("designWidth", raw(this.designWidth));
        properties.setProperty("designHeight", raw(this.designHeight));
        properties.setProperty("displayScaleCap", raw(this.displayScaleCap));
        properties.setProperty("windowRadius", raw(this.windowRadius));
        properties.setProperty("fontSize", raw(this.fontSize));
        properties.setProperty("textScale", raw(this.textScale));
        properties.setProperty("valueScale", raw(this.valueScale));
        properties.setProperty("miniScale", raw(this.miniScale));
        properties.setProperty("sidebarWidth", raw(this.sidebarWidth));
        properties.setProperty("sidebarXPadding", raw(this.sidebarXPadding));
        properties.setProperty("sidebarTopPadding", raw(this.sidebarTopPadding));
        properties.setProperty("sidebarEntrySpacing", raw(this.sidebarEntrySpacing));
        properties.setProperty("sidebarSubEntrySpacing", raw(this.sidebarSubEntrySpacing));
        properties.setProperty("sidebarFooterInset", raw(this.sidebarFooterInset));
        properties.setProperty("contentXOffset", raw(this.contentXOffset));
        properties.setProperty("contentYOffset", raw(this.contentYOffset));
        properties.setProperty("contentRightPadding", raw(this.contentRightPadding));
        properties.setProperty("columnGap", raw(this.columnGap));
        properties.setProperty("topbarYOffset", raw(this.topbarYOffset));
        properties.setProperty("topbarButtonSize", raw(this.topbarButtonSize));
        properties.setProperty("topbarSelectorHeight", raw(this.topbarSelectorHeight));
        properties.setProperty("topbarPresetWidth", raw(this.topbarPresetWidth));
        properties.setProperty("topbarModeWidth", raw(this.topbarModeWidth));
        properties.setProperty("topbarGap", raw(this.topbarGap));
        properties.setProperty("panelHeaderOffset", raw(this.panelHeaderOffset));
        properties.setProperty("panelRowStep", raw(this.panelRowStep));
        properties.setProperty("panelStackGap", raw(this.panelStackGap));
        properties.setProperty("panelRadius", raw(this.panelRadius));
        properties.setProperty("logoTitleScale", raw(this.logoTitleScale));
        properties.setProperty("logoSubtitleScale", raw(this.logoSubtitleScale));
        properties.setProperty("logoTextXOffset", raw(this.logoTextXOffset));
        properties.setProperty("logoTitleYOffset", raw(this.logoTitleYOffset));
        properties.setProperty("logoSubtitleYOffset", raw(this.logoSubtitleYOffset));
        properties.setProperty("footerNameScale", raw(this.footerNameScale));
        properties.setProperty("footerStatusScale", raw(this.footerStatusScale));
        properties.setProperty("footerHeight", raw(this.footerHeight));
        properties.setProperty("footerBottomInset", raw(this.footerBottomInset));
        properties.setProperty("footerTextXOffset", raw(this.footerTextXOffset));
        properties.setProperty("footerAvatarRadius", raw(this.footerAvatarRadius));
        properties.setProperty("dropdownWidth", raw(this.dropdownWidth));
        properties.setProperty("sliderTrackWidth", raw(this.sliderTrackWidth));
        properties.setProperty("menuOffsetX", raw(this.menuOffsetX));
        properties.setProperty("menuOffsetY", raw(this.menuOffsetY));
        properties.setProperty("debugOverlayX", raw(this.debugOverlayX));
        properties.setProperty("debugOverlayY", raw(this.debugOverlayY));
        return properties;
    }

    private static double read(final Properties properties, final String key, final double fallback) {
        final String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (final NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String format(final double value) {
        if (Double.isNaN(value)) {
            return "Double.NaN";
        }
        return String.format(Locale.US, "%.1fD", value);
    }

    private static String raw(final double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        return String.format(Locale.US, "%.4f", value);
    }
}
