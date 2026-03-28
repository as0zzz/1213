package dev.mdk2.client.settings;

import dev.mdk2.client.util.MathUtil;

public class NumberSetting extends Setting<Double> {
    private final double minimum;
    private final double maximum;
    private final double increment;

    public NumberSetting(final String name, final double value, final double minimum, final double maximum, final double increment) {
        super(name, Double.valueOf(value));
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    public void setValue(final double value) {
        final double clamped = MathUtil.clamp(value, this.minimum, this.maximum);
        setValueInternal(Double.valueOf(MathUtil.roundToStep(clamped, this.increment)));
    }

    public double getMinimum() {
        return this.minimum;
    }

    public double getMaximum() {
        return this.maximum;
    }

    public double getIncrement() {
        return this.increment;
    }

    public double getNormalizedValue() {
        return (getValue().doubleValue() - this.minimum) / (this.maximum - this.minimum);
    }
}
