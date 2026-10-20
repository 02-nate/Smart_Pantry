package com.example.smartpantrymanager.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Unit tests for {@link RecipeMatcher}. */
public class RecipeMatcherTest {

    private static final LocalDate TODAY = LocalDate.of(2025, 6, 15);

    private static PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(name, quantity, unit, null);
    }

    private static PantryItem item(String name, double quantity, String unit, String expiry) {
        return new PantryItem(name, quantity, unit, expiry);
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private static Recipe recipe(String name, RecipeIngredient... ingredients) {
        return new Recipe(1, name, "Cook.", new ArrayList<>(Arrays.asList(ingredients)));
    }

    /** Five-ingredient recipe shared by the classification tests. */
    private static Recipe fiveIngredientRecipe() {
        return recipe("Test Stew",
                ing("onion", 1, "pcs"),
                ing("tomato", 2, "pcs"),
                ing("carrot", 2, "pcs"),
                ing("rice", 250, "g"),
                ing("oil", 30, "ml"));
    }

    private static RecipeMatcher.MatchResults run(List<PantryItem> pantry, Recipe... recipes) {
        return RecipeMatcher.match(pantry, Arrays.asList(recipes), TODAY);
    }

    private static boolean contains(List<MatchResult> results, String recipeName) {
        for (MatchResult result : results) {
            if (result.getRecipe().getName().equals(recipeName)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void allIngredientsPresent_isStrictMatch() {
        List<PantryItem> pantry = Arrays.asList(
                item("onion", 1, "pcs"), item("tomato", 2, "pcs"), item("carrot", 2, "pcs"),
                item("rice", 250, "g"), item("oil", 30, "ml"));

        RecipeMatcher.MatchResults results = run(pantry, fiveIngredientRecipe());

        assertTrue(contains(results.getStrict(), "Test Stew"));
        assertFalse(contains(results.getAlmostThere(), "Test Stew"));
    }

    @Test
    public void fourOfFivePresent_isAlmostThereOnly() {
        List<PantryItem> pantry = Arrays.asList(
                item("onion", 1, "pcs"), item("tomato", 2, "pcs"), item("carrot", 2, "pcs"),
                item("rice", 250, "g"));

        RecipeMatcher.MatchResults results = run(pantry, fiveIngredientRecipe());

        assertFalse(contains(results.getStrict(), "Test Stew"));
        assertTrue(contains(results.getAlmostThere(), "Test Stew"));
        assertEquals(Collections.singletonList("oil"), results.getAlmostThere().get(0).getMissing());
    }

    @Test
    public void threeOfFivePresent_isInNeitherList() {
        List<PantryItem> pantry = Arrays.asList(
                item("onion", 1, "pcs"), item("tomato", 2, "pcs"), item("carrot", 2, "pcs"));

        RecipeMatcher.MatchResults results = run(pantry, fiveIngredientRecipe());

        assertFalse(contains(results.getStrict(), "Test Stew"));
        assertFalse(contains(results.getAlmostThere(), "Test Stew"));
    }

    @Test
    public void pluralPantryName_satisfiesSingularRecipeName() {
        Recipe r = recipe("Salad", ing("tomato", 2, "pcs"));

        RecipeMatcher.MatchResults results =
                run(Collections.singletonList(item("Tomatoes", 3, "pcs")), r);

        assertTrue(contains(results.getStrict(), "Salad"));
    }

    @Test
    public void kilograms_convertToGrams() {
        Recipe r = recipe("Pap", ing("maize meal", 500, "g"));

        RecipeMatcher.MatchResults results =
                run(Collections.singletonList(item("maize meal", 1, "kg")), r);

        assertTrue(contains(results.getStrict(), "Pap"));
    }

    @Test
    public void insufficientQuantity_isNotSatisfied() {
        Recipe r = recipe("Pap", ing("maize meal", 500, "g"));

        RecipeMatcher.MatchResults results =
                run(Collections.singletonList(item("maize meal", 400, "g")), r);

        assertFalse(contains(results.getStrict(), "Pap"));
    }

    @Test
    public void duplicatePantryRows_areSummed() {
        Recipe r = recipe("Pap", ing("maize meal", 500, "g"));
        List<PantryItem> pantry = Arrays.asList(
                item("maize meal", 250, "g"), item("maize meal", 250, "g"));

        RecipeMatcher.MatchResults results = run(pantry, r);

        assertTrue(contains(results.getStrict(), "Pap"));
    }

    @Test
    public void expiredItem_isIgnored() {
        Recipe r = recipe("Toast", ing("bread slice", 2, "pcs"));
        List<PantryItem> pantry = Collections.singletonList(
                item("bread slice", 4, "pcs", TODAY.minusDays(1).toString()));

        RecipeMatcher.MatchResults results = run(pantry, r);

        assertFalse(contains(results.getStrict(), "Toast"));
    }

    @Test
    public void countRequirement_notSatisfiedByMass() {
        Recipe r = recipe("Omelette", ing("egg", 2, "pcs"));

        RecipeMatcher.MatchResults results =
                run(Collections.singletonList(item("egg", 500, "g")), r);

        assertFalse(contains(results.getStrict(), "Omelette"));
    }

    @Test
    public void water_isNeverRequired() {
        Recipe r = recipe("Pap", ing("maize meal", 500, "g"), ing("water", 1000, "ml"));

        RecipeMatcher.MatchResults results =
                run(Collections.singletonList(item("maize meal", 500, "g")), r);

        assertTrue(contains(results.getStrict(), "Pap"));
    }

    @Test
    public void recipeWithNoIngredients_isNeverSuggested() {
        Recipe r = recipe("Nothing");

        RecipeMatcher.MatchResults results =
                run(Collections.singletonList(item("onion", 1, "pcs")), r);

        assertFalse(contains(results.getStrict(), "Nothing"));
        assertFalse(contains(results.getAlmostThere(), "Nothing"));
    }
}
