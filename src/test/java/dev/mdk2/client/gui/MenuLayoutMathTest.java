package dev.mdk2.client.gui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MenuLayoutMathTest {
    @Test
    public void miscRowCountKeepsOnlyDiscordRpcInGeneralPanel() {
        assertEquals(1, MenuLayoutMath.miscPanelRowCount("GENERAL"));
        assertEquals(0, MenuLayoutMath.miscPanelRowCount("TOOLS"));
    }

    @Test
    public void standardSelectorXAlignsDropdownToPanelRightEdge() {
        assertEquals(298.0D, MenuLayoutMath.standardSelectorX(100.0D, 300.0D, 90.0D), 0.0001D);
    }

    @Test
    public void visualModelColorSwatchSitsJustLeftOfSelector() {
        assertEquals(276.0D, MenuLayoutMath.visualModelColorX(298.0D), 0.0001D);
    }

    @Test
    public void profilePopupValueAndChevronStayTightToRightEdge() {
        final double popupX = 100.0D;
        final double popupWidth = 208.0D;

        assertEquals(288.0D, MenuLayoutMath.profilePopupChevronX(popupX, popupWidth), 0.0001D);
        assertEquals(222.0D, MenuLayoutMath.profilePopupValueX(popupX, popupWidth, 50.0D), 0.0001D);
    }

    @Test
    public void playersPageMovesEffectsBelowLeftStackAndKeepsPreviewAtTopRight() {
        assertEquals(120.0D, MenuLayoutMath.playersPreviewY(120.0D), 0.0001D);
        assertEquals(428.0D, MenuLayoutMath.playersEffectsY(120.0D, 132.0D, 156.0D, 10.0D), 0.0001D);
    }

    @Test
    public void previewStageUsesCompactTopBarAndNoBottomSliders() {
        assertEquals(234.0D, MenuLayoutMath.previewStageY(200.0D), 0.0001D);
        assertEquals(268.0D, MenuLayoutMath.previewStageHeight(320.0D), 0.0001D);
    }

    @Test
    public void topbarActionLabelCentersTextWithinButton() {
        assertEquals(131.0D, MenuLayoutMath.topbarActionLabelX(100.0D, 90.0D, 28.0D), 0.0001D);
    }

    @Test
    public void searchFieldUsesSharedIconAndTextOffsets() {
        assertEquals(36.0D, MenuLayoutMath.searchFieldIconX(20.0D), 0.0001D);
        assertEquals(52.0D, MenuLayoutMath.searchFieldIconY(42.0D, 32.0D, 12.0D), 0.0001D);
        assertEquals(46.0D, MenuLayoutMath.searchFieldTextX(20.0D), 0.0001D);
    }

    @Test
    public void searchPlaceholderCentersIconAndTextAsSingleGroup() {
        assertEquals(90.0D, MenuLayoutMath.searchPlaceholderIconX(20.0D, 200.0D, 40.0D), 0.0001D);
        assertEquals(110.0D, MenuLayoutMath.searchPlaceholderTextX(20.0D, 200.0D, 40.0D), 0.0001D);
    }

    @Test
    public void popupBindPillStaysCompactAndRightAligned() {
        assertEquals(84.0D, MenuLayoutMath.popupBindPillWidth(240.0D, 62.0D), 0.0001D);
    }

    @Test
    public void popupControlTextCentersWithinRowsAndPills() {
        assertEquals(9.5D, MenuLayoutMath.popupRowTextY(4.0D, 22.0D), 0.0001D);
        assertEquals(130.0D, MenuLayoutMath.popupPillTextX(100.0D, 80.0D, 20.0D), 0.0001D);
        assertEquals(10.0D, MenuLayoutMath.popupPillTextY(4.0D), 0.0001D);
        assertEquals(9.0D, MenuLayoutMath.popupSwatchY(4.0D, 22.0D), 0.0001D);
    }

    @Test
    public void popupViewportConvertsVirtualPopupAreaIntoScreenSpace() {
        assertEquals(180.0D, MenuLayoutMath.popupViewportX(100.0D, 40.0D, 2.0D), 0.0001D);
        assertEquals(130.0D, MenuLayoutMath.popupViewportY(80.0D, 25.0D, 2.0D), 0.0001D);
        assertEquals(300.0D, MenuLayoutMath.popupViewportSize(150.0D, 2.0D), 0.0001D);
    }

    @Test
    public void popupClampUsesAbsoluteMenuBoundsRatherThanLocalWidthOnly() {
        assertEquals(860.0D, MenuLayoutMath.popupRightBound(180.0D, 820.0D, 128.0D, 12.0D), 0.0001D);
        assertEquals(196.0D, MenuLayoutMath.popupLeftBound(180.0D, 16.0D), 0.0001D);
        assertEquals(860.0D, MenuLayoutMath.popupClampX(1030.0D, 128.0D, 180.0D, 820.0D, 12.0D), 0.0001D);
        assertEquals(192.0D, MenuLayoutMath.popupClampX(120.0D, 128.0D, 180.0D, 820.0D, 12.0D), 0.0001D);
    }

    @Test
    public void popupClampYRespectsAbsoluteMenuTopAndBottomEdges() {
        assertEquals(96.0D, MenuLayoutMath.popupTopBound(80.0D, 16.0D), 0.0001D);
        assertEquals(424.0D, MenuLayoutMath.popupBottomBound(80.0D, 520.0D, 160.0D, 16.0D), 0.0001D);
        assertEquals(96.0D, MenuLayoutMath.popupClampY(20.0D, 80.0D, 520.0D, 160.0D, 16.0D), 0.0001D);
        assertEquals(424.0D, MenuLayoutMath.popupClampY(490.0D, 80.0D, 520.0D, 160.0D, 16.0D), 0.0001D);
    }

    @Test
    public void contentBackplateCoversWholeContentAreaWithSoftBleed() {
        assertEquals(484.0D, MenuLayoutMath.contentBackplateX(280.0D, 168.0D, 54.0D, 18.0D), 0.0001D);
        assertEquals(124.0D, MenuLayoutMath.contentBackplateY(80.0D, 62.0D, 18.0D), 0.0001D);
        assertEquals(762.0D, MenuLayoutMath.contentBackplateWidth(980.0D, 168.0D, 54.0D, 32.0D, 18.0D), 0.0001D);
        assertEquals(554.0D, MenuLayoutMath.contentBackplateHeight(620.0D, 62.0D, 22.0D, 18.0D), 0.0001D);
    }
}
