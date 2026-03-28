package dev.mdk2.client.settings;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(final String name, final boolean value) {
        super(name, Boolean.valueOf(value));
    }

    public void toggle() {
        setValue(!getValue().booleanValue());
    }

    public void setValue(final boolean value) {
        setValueInternal(Boolean.valueOf(value));
    }
}
