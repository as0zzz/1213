package dev.mdk2.client.gui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MenuDebugOverlayStateTest {
    @Test
    public void defaultPositionAnchorsOverlayToTopRight() {
        final MenuDebugOverlayState state = MenuDebugOverlayState.defaultFor(1920.0D, 1080.0D, 350.0D, 338.0D);

        assertEquals(1546.0D, state.x, 0.0001D);
        assertEquals(24.0D, state.y, 0.0001D);
    }

    @Test
    public void clampKeepsOverlayInsideScreenPadding() {
        final MenuDebugOverlayState state = new MenuDebugOverlayState(9999.0D, -120.0D);

        state.clampInto(1280.0D, 720.0D, 350.0D, 338.0D, 12.0D);

        assertEquals(918.0D, state.x, 0.0001D);
        assertEquals(12.0D, state.y, 0.0001D);
    }

    @Test
    public void softClampAllowsPartialOffscreenButKeepsHeaderReachable() {
        final MenuDebugOverlayState state = new MenuDebugOverlayState(-999.0D, 999.0D);

        state.clampSoft(1280.0D, 720.0D, 350.0D, 338.0D, 72.0D, 42.0D);

        assertEquals(-278.0D, state.x, 0.0001D);
        assertEquals(678.0D, state.y, 0.0001D);
    }
}
