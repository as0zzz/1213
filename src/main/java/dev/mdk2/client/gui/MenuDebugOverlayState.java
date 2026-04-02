package dev.mdk2.client.gui;

public final class MenuDebugOverlayState {
    public double x;
    public double y;

    public MenuDebugOverlayState(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public static MenuDebugOverlayState defaultFor(final double screenWidth, final double screenHeight, final double overlayWidth, final double overlayHeight) {
        final MenuDebugOverlayState state = new MenuDebugOverlayState(screenWidth - overlayWidth - 24.0D, 24.0D);
        state.clampInto(screenWidth, screenHeight, overlayWidth, overlayHeight, 12.0D);
        return state;
    }

    public void clampInto(final double screenWidth, final double screenHeight, final double overlayWidth, final double overlayHeight, final double padding) {
        this.x = Math.max(padding, Math.min(this.x, screenWidth - overlayWidth - padding));
        this.y = Math.max(padding, Math.min(this.y, screenHeight - overlayHeight - padding));
    }

    public void unclamped() {
    }

    public void clampSoft(final double screenWidth, final double screenHeight, final double overlayWidth, final double overlayHeight, final double visibleWidth, final double visibleHeaderHeight) {
        this.x = Math.max(visibleWidth - overlayWidth, Math.min(this.x, screenWidth - visibleWidth));
        this.y = Math.max(0.0D, Math.min(this.y, screenHeight - visibleHeaderHeight));
    }
}
