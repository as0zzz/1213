package dev.mdk2.client.discord;

final class DiscordRpcText {
    private static final String CLIENT_NAME = "Mdk2 Client";
    private static final String GENERIC_DETAILS = "Minecraft utility client";

    private DiscordRpcText() {
    }

    static String buildState() {
        return "Cheat: " + CLIENT_NAME;
    }

    static String buildDetails(final boolean showUsername, final String username) {
        if (showUsername && username != null) {
            final String normalized = username.trim();
            if (!normalized.isEmpty()) {
                return "Username: " + normalized;
            }
        }
        return GENERIC_DETAILS;
    }

    static String buildLargeImageText() {
        return CLIENT_NAME;
    }
}
