package com.example.smartpantrymanager.matching;

/**
 * Supported units. Each unit belongs to a {@link Dimension} and has a factor that converts
 * it to that dimension's base unit (g for mass, ml for volume, pcs for count).
 */
public enum UnitType {
    G("g", Dimension.MASS, 1),
    KG("kg", Dimension.MASS, 1000),
    ML("ml", Dimension.VOLUME, 1),
    L("L", Dimension.VOLUME, 1000),
    TSP("tsp", Dimension.VOLUME, 5),
    TBSP("tbsp", Dimension.VOLUME, 15),
    CUP("cup", Dimension.VOLUME, 250),
    PCS("pcs", Dimension.COUNT, 1);

    private final String label;
    private final Dimension dimension;
    private final double factor;

    UnitType(String label, Dimension dimension, double factor) {
        this.label = label;
        this.dimension = dimension;
        this.factor = factor;
    }

    /** Short label shown in the UI and stored in the database, e.g. "kg". */
    public String getLabel() {
        return label;
    }

    public Dimension getDimension() {
        return dimension;
    }

    /** Multiplier that converts one of this unit into the base unit of its dimension. */
    public double getFactor() {
        return factor;
    }

    /** Converts a quantity in this unit to base units. */
    public double toBase(double quantity) {
        return quantity * factor;
    }
}
