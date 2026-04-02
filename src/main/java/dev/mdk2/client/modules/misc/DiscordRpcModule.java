package dev.mdk2.client.modules.misc;

import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;

public class DiscordRpcModule extends Module {
    private final BooleanSetting showUsername;
    private final BooleanSetting showServer;
    private final BooleanSetting showDimension;

    public DiscordRpcModule() {
        super("Discord RPC", "Syncs your current Mdk2 session into Discord Rich Presence.", Category.MISC);
        this.showUsername = register(new BooleanSetting("Show Username", true));
        this.showServer = register(new BooleanSetting("Show Server", true));
        this.showDimension = register(new BooleanSetting("Show Dimension", true));
    }

    @Override
    public void onEnable() {
        final ClientRuntime runtime = ClientRuntime.getInstance();
        if (runtime != null) {
            runtime.getDiscordRpcService().start();
        }
    }

    @Override
    public void onDisable() {
        final ClientRuntime runtime = ClientRuntime.getInstance();
        if (runtime != null) {
            runtime.getDiscordRpcService().shutdown();
        }
    }

    @Override
    public void onTick() {
        final ClientRuntime runtime = ClientRuntime.getInstance();
        if (runtime != null) {
            runtime.getDiscordRpcService().update(this);
        }
    }

    public boolean getShowUsername() {
        return this.showUsername.getValue().booleanValue();
    }

    public boolean getShowServer() {
        return this.showServer.getValue().booleanValue();
    }

    public boolean getShowDimension() {
        return this.showDimension.getValue().booleanValue();
    }
}
