package dev.mdk2.client.gui;

public final class FunctionPopupLayout {
    private static final double COMPACT_HEADER_HEIGHT = 48.0D;

    public final boolean twoColumn;
    public final double width;
    public final int leftCount;
    public final int rightCount;

    private FunctionPopupLayout(final boolean twoColumn, final double width, final int leftCount, final int rightCount) {
        this.twoColumn = twoColumn;
        this.width = width;
        this.leftCount = leftCount;
        this.rightCount = rightCount;
    }

    public static FunctionPopupLayout forSettingCount(final int settingCount, final double singleColumnWidth) {
        return new FunctionPopupLayout(false, singleColumnWidth, Math.max(0, settingCount), 0);
    }

    public static double compactHeaderHeight() {
        return COMPACT_HEADER_HEIGHT;
    }
}
