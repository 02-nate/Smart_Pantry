package com.example.smartpantrymanager.matching;

import com.example.smartpantrymanager.model.Recipe;

import java.util.Collections;
import java.util.List;

/** A recipe paired with the names of the ingredients the pantry is missing or short of. */
public final class MatchResult {

    private final Recipe recipe;
    private final List<String> missing;

    public MatchResult(Recipe recipe, List<String> missing) {
        this.recipe = recipe;
        this.missing = Collections.unmodifiableList(missing);
    }

    public Recipe getRecipe() {
        return recipe;
    }

    /** Names of missing or insufficient ingredients, as written in the recipe. */
    public List<String> getMissing() {
        return missing;
    }

    /** Returns true when every ingredient is available in sufficient quantity. */
    public boolean isComplete() {
        return missing.isEmpty();
    }
}
