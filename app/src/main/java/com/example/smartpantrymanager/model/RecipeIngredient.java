package com.example.smartpantrymanager.model;

/** One ingredient line of a recipe, such as "onion 1 pcs". */
public class RecipeIngredient {

    private long id;
    private long recipeId;
    private String name;
    private double quantity;
    private String unit;

    /** Creates an empty ingredient; used when reading rows from the database. */
    public RecipeIngredient() {
    }

    /** Creates an ingredient that is not yet linked to a stored recipe. */
    public RecipeIngredient(String name, double quantity, String unit) {
        this(0, 0, name, quantity, unit);
    }

    /** Creates an ingredient with known database ids. */
    public RecipeIngredient(long id, long recipeId, String name, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
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
}
