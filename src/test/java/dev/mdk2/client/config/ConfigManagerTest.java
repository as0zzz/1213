package dev.mdk2.client.config;

import dev.mdk2.client.gui.MenuProfileSettings;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BindSetting;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ColorSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigManagerTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void saveAndLoadRoundTripsModuleStateAndSettings() throws Exception {
        final DummyModule module = new DummyModule();
        module.setEnabled(true);
        module.enabledSetting.setValue(true);
        module.modeSetting.next();
        module.rangeSetting.setValue(7.5D);
        module.colorSetting.setColor(0xCC55AAFF);
        module.getBind().setKey(GLFW.GLFW_KEY_K);

        final File configDir = this.temporaryFolder.newFolder("configs");
        final ConfigManager manager = new ConfigManager(configDir.toPath());
        final List<Module> modules = Arrays.<Module>asList(module);

        manager.save("Unnamed", modules);

        module.setEnabled(false);
        module.enabledSetting.setValue(false);
        module.modeSetting.previous();
        module.rangeSetting.setValue(2.0D);
        module.colorSetting.setColor(0xFFFFFFFF);
        module.getBind().setKey(BindSetting.NONE);

        manager.load("Unnamed", modules);

        assertTrue(module.isEnabled());
        assertTrue(module.enabledSetting.getValue().booleanValue());
        assertEquals("B", module.modeSetting.getValue());
        assertEquals(7.5D, module.rangeSetting.getValue().doubleValue(), 0.0001D);
        assertEquals(0xCC55AAFF, module.colorSetting.getColor());
        assertEquals(GLFW.GLFW_KEY_K, module.getBind().getKey());
    }

    @Test
    public void listConfigNamesReturnsSortedFileNamesWithoutExtensions() throws Exception {
        final File configDir = this.temporaryFolder.newFolder("configs");
        final ConfigManager manager = new ConfigManager(configDir.toPath());
        manager.save("Beta", Arrays.<Module>asList(new DummyModule()));
        manager.save("Alpha", Arrays.<Module>asList(new DummyModule()));

        assertEquals(Arrays.asList("Alpha", "Beta"), manager.listConfigNames());
    }

    @Test
    public void createNextNameSkipsExistingIndices() throws Exception {
        final File configDir = this.temporaryFolder.newFolder("configs");
        final ConfigManager manager = new ConfigManager(configDir.toPath());
        manager.save("Unnamed", Arrays.<Module>asList(new DummyModule()));
        manager.save("Config 2", Arrays.<Module>asList(new DummyModule()));

        assertEquals("Config 3", manager.createNextName());
    }

    @Test
    public void deleteRemovesConfigAndFallsBackToRemainingEntry() throws Exception {
        final File configDir = this.temporaryFolder.newFolder("configs");
        final ConfigManager manager = new ConfigManager(configDir.toPath());
        manager.save("Alpha", Arrays.<Module>asList(new DummyModule()));
        manager.save("Beta", Arrays.<Module>asList(new DummyModule()));
        manager.setSelectedConfigName("Beta");

        manager.delete("Beta");

        assertEquals(Arrays.asList("Alpha"), manager.listConfigNames());
        assertEquals("Alpha", manager.getSelectedConfigName());
    }

    @Test
    public void sanitizeNameFallsBackForBlankInput() {
        assertEquals("Unnamed", ConfigManager.sanitizeName("   "));
        assertEquals("Test_Name", ConfigManager.sanitizeName("Test:/Name"));
    }

    @Test
    public void saveAndLoadRoundTripsMenuProfileState() throws Exception {
        final DummyModule module = new DummyModule();
        final File configDir = this.temporaryFolder.newFolder("configs");
        final ConfigManager manager = new ConfigManager(configDir.toPath());
        final MenuProfileSettings settings = MenuProfileSettings.defaults();
        settings.languageIndex = 1;
        settings.menuScaleIndex = 6;
        settings.safeModeIndex = 2;
        settings.style = "Glass";

        manager.save("Alpha", Arrays.<Module>asList(module), settings);
        final MenuProfileSettings loaded = manager.loadProfile("Alpha", MenuProfileSettings.defaults());

        assertEquals(1, loaded.languageIndex);
        assertEquals(6, loaded.menuScaleIndex);
        assertEquals(2, loaded.safeModeIndex);
        assertEquals("Glass", loaded.style);
    }

    private static final class DummyModule extends Module {
        private final BooleanSetting enabledSetting;
        private final ModeSetting modeSetting;
        private final NumberSetting rangeSetting;
        private final ColorSetting colorSetting;

        private DummyModule() {
            super("Dummy", "Dummy test module", Category.MISC);
            this.enabledSetting = register(new BooleanSetting("Enabled Setting", false));
            this.modeSetting = register(new ModeSetting("Mode", "A", "A", "B", "C"));
            this.rangeSetting = register(new NumberSetting("Range", 2.0D, 0.0D, 10.0D, 0.1D));
            this.colorSetting = register(new ColorSetting("Color", 0xFFFFFFFF));
        }
    }
}
