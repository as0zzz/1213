package dev.mdk2.client.discord;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DiscordRpcTextTest {
    @Test
    public void stateAlwaysShowsClientNameInsteadOfServerContext() {
        assertEquals("Cheat: Mdk2 Client", DiscordRpcText.buildState());
    }

    @Test
    public void detailsCanShowUsernameWhenEnabled() {
        assertEquals("Username: panic", DiscordRpcText.buildDetails(true, "panic"));
    }

    @Test
    public void detailsFallBackToGenericClientLabel() {
        assertEquals("Minecraft utility client", DiscordRpcText.buildDetails(false, "panic"));
        assertEquals("Minecraft utility client", DiscordRpcText.buildDetails(true, ""));
    }
}
