package com.example.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/** A recipe with its ingredient list and newline-separated preparation steps. */
public class Recipe {

    private long id;
    private String name;
    private String steps;
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    /** Creates an empty recipe; used when reading rows from the database. */
    public Recipe() {
    }

    /** Creates a recipe with all of its fields. */
    public Recipe(long id, String name, String steps, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        setIngredients(ingredients);
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

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    /** Replaces the ingredient list; null is stored as an empty list. */
    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients == null ? new ArrayList<>() : ingredients;
    }

    /** Returns the steps as a list, skipping blank lines. */
    public List<String> getStepList() {
        List<String> result = new ArrayList<>();
        if (steps == null) {
            return result;
        }
        for (String line : steps.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
