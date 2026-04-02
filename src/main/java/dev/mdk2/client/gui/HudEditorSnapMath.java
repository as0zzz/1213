package dev.mdk2.client.gui;

final class HudEditorSnapMath {
    private static final double SNAP_DISTANCE = 6.0D;
    private static final double RELEASE_DISTANCE = 14.0D;

    private HudEditorSnapMath() {
    }

    static double guideX(final double screenWidth) {
        return screenWidth / 2.0D;
    }

    static double guideY(final double screenHeight) {
        return screenHeight / 2.0D;
    }

    static SnapResult snapAxis(final double position, final double size, final double screenSize, final boolean snapped) {
        final double centeredPosition = (screenSize - size) / 2.0D;
        final double distanceFromCenter = position + size / 2.0D - screenSize / 2.0D;
        if (snapped) {
            if (Math.abs(distanceFromCenter) <= RELEASE_DISTANCE) {
                return new SnapResult(centeredPosition, true);
            }
            return new SnapResult(position, false);
        }
        if (Math.abs(distanceFromCenter) <= SNAP_DISTANCE) {
            return new SnapResult(centeredPosition, true);
        }
        return new SnapResult(position, false);
    }

    static final class SnapResult {
        final double position;
        final boolean snapped;

        private SnapResult(final double position, final boolean snapped) {
            this.position = position;
            this.snapped = snapped;
        }
    }
}
