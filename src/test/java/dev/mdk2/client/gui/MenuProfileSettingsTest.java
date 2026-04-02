package dev.mdk2.client.gui;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class MenuProfileSettingsTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void saveAndLoadRoundTripsProfileChoices() throws Exception {
        final MenuProfileSettings settings = MenuProfileSettings.defaults();
        settings.languageIndex = 1;
        settings.menuScaleIndex = 5;
        settings.safeModeIndex = 2;
        settings.style = "Glass";
        final File file = this.temporaryFolder.newFile("menu-profile.properties");

        settings.save(file.toPath());
        final MenuProfileSettings loaded = MenuProfileSettings.load(file.toPath());

        assertEquals(1, loaded.languageIndex);
        assertEquals(5, loaded.menuScaleIndex);
        assertEquals(2, loaded.safeModeIndex);
        assertEquals("Glass", loaded.style);
    }
}
