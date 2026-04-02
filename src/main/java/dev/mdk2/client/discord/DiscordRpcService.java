package dev.mdk2.client.discord;

import dev.mdk2.client.modules.misc.DiscordRpcModule;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

public final class DiscordRpcService {
    private static final String APPLICATION_ID = "620780960146702346";

    private boolean initialized;
    private long startedAt;

    public void start() {
        if (this.initialized) {
            return;
        }
        try {
            final DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().build();
            DiscordRPC.discordInitialize(APPLICATION_ID, handlers, true);
            this.startedAt = System.currentTimeMillis() / 1000L;
            this.initialized = true;
        } catch (final Throwable ignored) {
            this.initialized = false;
        }
    }

    public void shutdown() {
        if (!this.initialized) {
            return;
        }
        try {
            DiscordRPC.discordClearPresence();
            DiscordRPC.discordShutdown();
        } catch (final Throwable ignored) {
        }
        this.initialized = false;
    }

    public void update(final DiscordRpcModule module) {
        if (!module.isEnabled()) {
            shutdown();
            return;
        }
        start();
        if (!this.initialized) {
            return;
        }
        try {
            final DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(DiscordRpcText.buildState());
            builder.setDetails(buildDetails(module));
            builder.setBigImage("logo", DiscordRpcText.buildLargeImageText());
            builder.setStartTimestamps(this.startedAt);
            DiscordRPC.discordUpdatePresence(builder.build());
            DiscordRPC.discordRunCallbacks();
        } catch (final Throwable ignored) {
            shutdown();
        }
    }

    private String buildDetails(final DiscordRpcModule module) {
        final PlayerEntity player = Minecraft.getInstance().player;
        return DiscordRpcText.buildDetails(module.getShowUsername(), player == null ? null : player.getName().getString());
    }
}
