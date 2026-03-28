package dev.mdk2.client;

import dev.mdk2.client.core.ClientRuntime;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Mdk2ClientMod.MOD_ID)
public class Mdk2ClientMod {
    public static final String MOD_ID = "mdk2client";

    public Mdk2ClientMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(ClientRuntime::bootstrap);
    }
}
