package dev.mdk2.client.gui;

import dev.mdk2.client.modules.combat.AimAssistModule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MenuPanelCatalogTest {
    @Test
    public void rageAssistPanelNoLongerIncludesAimAssist() {
        final List<Class<? extends dev.mdk2.client.modules.Module>> modules = MenuPanelCatalog.modulesFor("RAGE", null, "ASSIST");

        assertFalse(modules.contains(AimAssistModule.class));
    }

    @Test
    public void legitMainPanelKeepsAimAssist() {
        final List<Class<? extends dev.mdk2.client.modules.Module>> modules = MenuPanelCatalog.modulesFor("LEGIT", null, "MAIN");

        assertTrue(modules.contains(AimAssistModule.class));
    }
}
