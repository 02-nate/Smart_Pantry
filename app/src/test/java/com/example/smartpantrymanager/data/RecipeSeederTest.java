package com.example.smartpantrymanager.data;

import static org.junit.Assert.assertEquals;

import com.example.smartpantrymanager.matching.MatchResult;
import com.example.smartpantrymanager.matching.RecipeMatcher;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Checks the seed data: the demo pantry must give the documented suggestions. */
public class RecipeSeederTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    private static List<String> names(List<MatchResult> results) {
        List<String> names = new ArrayList<>();
        for (MatchResult result : results) {
            names.add(result.getRecipe().getName());
        }
        return names;
    }

    @Test
    public void seedsTwentyRecipes() {
        assertEquals(20, RecipeSeeder.recipes().size());
    }

    @Test
    public void demoPantry_givesDocumentedStrictAndAlmostThereLists() {
        RecipeMatcher.MatchResults results = RecipeMatcher.match(
                RecipeSeeder.demoPantry(), RecipeSeeder.recipes(), TODAY);

        assertEquals(Arrays.asList(
                        "Chakalaka", "Egg and Tomato Scramble", "Pap and Tomato Gravy"),
                names(results.getStrict()));
        assertEquals(Arrays.asList(
                        "Cheese Omelette", "Fried Cabbage", "Morogo and Pap"),
                names(results.getAlmostThere()));
    }
}
