package com.example.smartpantrymanager.util;

import com.example.smartpantrymanager.matching.Dimension;
import com.example.smartpantrymanager.matching.IngredientNormalizer;
import com.example.smartpantrymanager.matching.UnitType;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats quantities for display. Mass and volume are converted to base units (g / ml);
 * with auto-scale on, amounts of 1000 or more are shown in kg / L instead.
 */
public final class QuantityFormatter {

    private static final double SCALE_THRESHOLD = 1000;

    private QuantityFormatter() {
    }

    /**
     * Returns a display string such as "500 g", "1.5 kg" or "3 pcs".
     *
     * @param autoScale true to show kg / L for large amounts, false to always show g / ml
     */
    public static String format(double quantity, String unit, boolean autoScale) {
        UnitType type;
        try {
            type = IngredientNormalizer.parseUnit(unit);
        } catch (IllegalArgumentException e) {
            // Unknown units are shown as stored rather than hidden.
            return formatNumber(quantity) + " " + unit;
        }
        if (type.getDimension() == Dimension.COUNT) {
            return formatNumber(quantity) + " " + type.getLabel();
        }

        double base = type.toBase(quantity);
        boolean isMass = type.getDimension() == Dimension.MASS;
        UnitType baseUnit = isMass ? UnitType.G : UnitType.ML;
        UnitType largeUnit = isMass ? UnitType.KG : UnitType.L;

        if (autoScale && base >= SCALE_THRESHOLD) {
            return formatNumber(base / largeUnit.getFactor()) + " " + largeUnit.getLabel();
        }
        return formatNumber(base) + " " + baseUnit.getLabel();
    }

    /** Formats a number with at most two decimals and no trailing zeros, e.g. 2, 1.5, 0.25. */
    public static String formatNumber(double value) {
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.getDefault()));
        return format.format(value);
    }
}
