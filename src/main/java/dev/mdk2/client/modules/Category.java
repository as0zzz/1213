package dev.mdk2.client.modules;

public enum Category {
    COMBAT("Combat"),
    VISUAL("Visual"),
    MOVEMENT("Movement"),
    MISC("Misc");

    private final String title;

    Category(final String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
