package com.example.smartpantrymanager.model;

/**
 * An ingredient the user has in their pantry. The name is stored exactly as typed;
 * normalisation only happens during recipe matching.
 */
public class PantryItem {

    private long id;
    private String name;
    private double quantity;
    private String unit;
    /** ISO date (yyyy-MM-dd), or null when the item has no expiry date. */
    private String expiryDate;

    /** Creates an empty item; used when reading rows from the database. */
    public PantryItem() {
    }

    /** Creates a new, unsaved item (id 0). */
    public PantryItem(String name, double quantity, String unit, String expiryDate) {
        this(0, name, quantity, unit, expiryDate);
    }

    /** Creates an item with a known database id. */
    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
