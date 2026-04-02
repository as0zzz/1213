package dev.mdk2.client.gui;

final class MenuLayoutMath {
    private static final double STANDARD_SELECTOR_RIGHT_INSET = 12.0D;
    private static final double VISUAL_MODEL_SELECTOR_RIGHT_INSET = 12.0D;
    private static final double VISUAL_MODEL_SWATCH_GAP = 10.0D;
    private static final double VISUAL_MODEL_SWATCH_SIZE = 12.0D;
    private static final double PROFILE_POPUP_CHEVRON_RIGHT_INSET = 20.0D;
    private static final double PROFILE_POPUP_VALUE_GAP = 16.0D;
    private static final double TOPBAR_ACTION_LABEL_VERTICAL_NUDGE = 0.0D;
    private static final double SEARCH_FIELD_ICON_LEFT_INSET = 16.0D;
    private static final double SEARCH_FIELD_TEXT_LEFT_INSET = 26.0D;
    private static final double SEARCH_PLACEHOLDER_GAP = 8.0D;
    private static final double SEARCH_ICON_SIZE = 12.0D;

    private MenuLayoutMath() {
    }

    static int miscPanelRowCount(final String title) {
        if ("GENERAL".equals(title)) {
            return 1;
        }
        if ("TOOLS".equals(title)) {
            return 0;
        }
        return 5;
    }

    static double standardSelectorX(final double panelX, final double panelWidth, final double dropdownWidth) {
        return panelX + panelWidth - dropdownWidth - STANDARD_SELECTOR_RIGHT_INSET;
    }

    static double visualModelColorX(final double selectorX) {
        return selectorX - VISUAL_MODEL_SWATCH_GAP - VISUAL_MODEL_SWATCH_SIZE;
    }

    static double visualModelSelectorX(final double panelX, final double panelWidth, final double dropdownWidth) {
        return panelX + panelWidth - dropdownWidth - VISUAL_MODEL_SELECTOR_RIGHT_INSET;
    }

    static double profilePopupChevronX(final double popupX, final double popupWidth) {
        return popupX + popupWidth - PROFILE_POPUP_CHEVRON_RIGHT_INSET;
    }

    static double profilePopupValueX(final double popupX, final double popupWidth, final double valueWidth) {
        return profilePopupChevronX(popupX, popupWidth) - PROFILE_POPUP_VALUE_GAP - valueWidth;
    }

    static double playersPreviewY(final double contentY) {
        return contentY;
    }

    static double playersEffectsY(final double contentY, final double enemyHeight, final double enemyModelHeight, final double stackGap) {
        return contentY + enemyHeight + stackGap + enemyModelHeight + stackGap;
    }

    static double previewStageY(final double panelY) {
        return panelY + 34.0D;
    }

    static double previewStageHeight(final double panelHeight) {
        return panelHeight - 52.0D;
    }

    static double topbarActionLabelX(final double buttonX, final double buttonWidth, final double textWidth) {
        return buttonX + (buttonWidth - textWidth) / 2.0D;
    }

    static double topbarActionLabelY(final double buttonY, final double buttonHeight) {
        return buttonY + Math.max(4.0D, (buttonHeight - 14.0D) / 2.0D) + TOPBAR_ACTION_LABEL_VERTICAL_NUDGE;
    }

    static double searchFieldIconX(final double fieldX) {
        return fieldX + SEARCH_FIELD_ICON_LEFT_INSET;
    }

    static double searchFieldIconY(final double fieldY, final double fieldHeight, final double iconSize) {
        return fieldY + (fieldHeight - iconSize) / 2.0D;
    }

    static double searchFieldTextX(final double fieldX) {
        return fieldX + SEARCH_FIELD_TEXT_LEFT_INSET;
    }

    static double searchPlaceholderIconX(final double fieldX, final double fieldWidth, final double textWidth) {
        return fieldX + (fieldWidth - (SEARCH_ICON_SIZE + SEARCH_PLACEHOLDER_GAP + textWidth)) / 2.0D;
    }

    static double searchPlaceholderTextX(final double fieldX, final double fieldWidth, final double textWidth) {
        return searchPlaceholderIconX(fieldX, fieldWidth, textWidth) + SEARCH_ICON_SIZE + SEARCH_PLACEHOLDER_GAP;
    }

    static double popupBindPillWidth(final double rowWidth, final double textWidth) {
        return Math.min(rowWidth * 0.40D, Math.max(72.0D, textWidth + 22.0D));
    }

    static double popupRowTextY(final double rowY, final double rowHeight) {
        return rowY + Math.max(3.5D, (rowHeight - 11.0D) / 2.0D);
    }

    static double popupPillTextX(final double pillX, final double pillWidth, final double textWidth) {
        return pillX + (pillWidth - textWidth) / 2.0D;
    }

    static double popupPillTextY(final double rowY) {
        return rowY + 6.0D;
    }

    static double popupSwatchY(final double rowY, final double rowHeight) {
        return rowY + (rowHeight - 12.0D) / 2.0D;
    }

    static double popupViewportX(final double originX, final double localX, final double scale) {
        return originX + localX * scale;
    }

    static double popupViewportY(final double originY, final double localY, final double scale) {
        return originY + localY * scale;
    }

    static double popupViewportSize(final double localSize, final double scale) {
        return localSize * scale;
    }

    static double popupLeftBound(final double menuX, final double inset) {
        return menuX + inset;
    }

    static double popupRightBound(final double menuX, final double menuWidth, final double popupWidth, final double inset) {
        return menuX + menuWidth - popupWidth - inset;
    }

    static double popupClampX(final double desiredX, final double popupWidth, final double menuX, final double menuWidth, final double inset) {
        return Math.max(popupLeftBound(menuX, inset), Math.min(desiredX, popupRightBound(menuX, menuWidth, popupWidth, inset)));
    }

    static double popupTopBound(final double menuY, final double inset) {
        return menuY + inset;
    }

    static double popupBottomBound(final double menuY, final double menuHeight, final double popupHeight, final double inset) {
        return menuY + menuHeight - popupHeight - inset;
    }

    static double popupClampY(final double desiredY, final double menuY, final double menuHeight, final double popupHeight, final double inset) {
        return Math.max(popupTopBound(menuY, inset), Math.min(desiredY, popupBottomBound(menuY, menuHeight, popupHeight, inset)));
    }

    static double contentBackplateX(final double menuX, final double sidebarWidth, final double contentXOffset, final double bleed) {
        return menuX + sidebarWidth + contentXOffset - bleed;
    }

    static double contentBackplateY(final double menuY, final double contentYOffset, final double bleed) {
        return menuY + contentYOffset - bleed;
    }

    static double contentBackplateWidth(final double menuWidth, final double sidebarWidth, final double contentXOffset, final double rightPadding, final double bleed) {
        return menuWidth - sidebarWidth - contentXOffset - rightPadding + bleed * 2.0D;
    }

    static double contentBackplateHeight(final double menuHeight, final double contentYOffset, final double bottomInset, final double bleed) {
        return menuHeight - contentYOffset - bottomInset + bleed;
    }
}
