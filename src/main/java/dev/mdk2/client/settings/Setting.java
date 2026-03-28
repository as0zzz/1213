package dev.mdk2.client.settings;

public abstract class Setting<T> {
    private final String name;
    private T value;

    protected Setting(final String name, final T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public T getValue() {
        return this.value;
    }

    protected void setValueInternal(final T value) {
        this.value = value;
    }
}
