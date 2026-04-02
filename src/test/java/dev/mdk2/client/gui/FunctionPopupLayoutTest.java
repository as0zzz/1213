package dev.mdk2.client.gui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FunctionPopupLayoutTest {
    @Test
    public void elevenSettingsOrFewerStaySingleColumn() {
        final FunctionPopupLayout layout = FunctionPopupLayout.forSettingCount(11, 194.0D);

        assertFalse(layout.twoColumn);
        assertEquals(194.0D, layout.width, 0.0001D);
        assertEquals(11, layout.leftCount);
        assertEquals(0, layout.rightCount);
    }

    @Test
    public void moreThanElevenSettingsStillStaySingleColumn() {
        final FunctionPopupLayout layout = FunctionPopupLayout.forSettingCount(12, 194.0D);

        assertFalse(layout.twoColumn);
        assertEquals(194.0D, layout.width, 0.0001D);
        assertEquals(12, layout.leftCount);
        assertEquals(0, layout.rightCount);
    }

    @Test
    public void largerCountsKeepWholeListInPrimaryColumn() {
        final FunctionPopupLayout layout = FunctionPopupLayout.forSettingCount(15, 194.0D);

        assertEquals(15, layout.leftCount);
        assertEquals(0, layout.rightCount);
    }

    @Test
    public void compactHeaderHeightLeavesRoomForTitleWithoutDescription() {
        assertEquals(48.0D, FunctionPopupLayout.compactHeaderHeight(), 0.0001D);
    }
}
