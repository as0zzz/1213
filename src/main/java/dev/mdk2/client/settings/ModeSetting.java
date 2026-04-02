package dev.mdk2.client.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(final String name, final String defaultValue, final String... modes) {
        super(name, defaultValue);
        this.modes = Collections.unmodifiableList(Arrays.asList(modes));
    }

    public List<String> getModes() {
        return this.modes;
    }

    public void next() {
        final int index = this.modes.indexOf(getValue());
        setValueInternal(this.modes.get((index + 1) % this.modes.size()));
    }

    public void previous() {
        final int index = this.modes.indexOf(getValue());
        setValueInternal(this.modes.get((index - 1 + this.modes.size()) % this.modes.size()));
    }

    public void setValue(final String value) {
        if (this.modes.contains(value)) {
            setValueInternal(value);
        }
    }
}
