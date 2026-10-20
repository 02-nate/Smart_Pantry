package com.example.smartpantrymanager.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Seed data inserted when the database is first created: the 20 built-in South African
 * recipes and the demo pantry. Units follow one convention: dry ingredients in g, liquids
 * in ml, countable items in pcs.
 */
public final class RecipeSeeder {

    private static final String G = "g";
    private static final String ML = "ml";
    private static final String PCS = "pcs";
    private static final String KG = "kg";
    private static final String L = "L";

    private RecipeSeeder() {
    }

    /** Inserts every seed recipe. Called from {@link DatabaseHelper#onCreate}. */
    public static void seed(SQLiteDatabase db) {
        for (Recipe recipe : recipes()) {
            insert(db, recipe);
        }
    }

    /**
     * Returns the seed recipes as plain objects (ids are 0). Kept free of database code so
     * the recipe data can be checked against the matcher in JVM unit tests.
     */
    public static List<Recipe> recipes() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(recipe("Chakalaka", steps(
                        "Heat the oil in a pot and fry the chopped onion until soft.",
                        "Add the curry powder and fry for one minute.",
                        "Add the grated carrots, diced green pepper and chopped tomatoes and cook for 10 minutes.",
                        "Stir in the baked beans and simmer for 5 minutes.",
                        "Serve hot or cold as a side dish."),
                ing("onion", 1, PCS), ing("tomato", 2, PCS), ing("carrot", 2, PCS),
                ing("green pepper", 1, PCS), ing("baked beans", 410, G),
                ing("curry powder", 10, G), ing("oil", 30, ML)));

        recipes.add(recipe("Pap and Tomato Gravy", steps(
                        "Bring the salted water to the boil in a heavy pot.",
                        "Stir in the maize meal, cover and cook on low heat for 25 minutes, stirring now and then.",
                        "Meanwhile fry the chopped onion in oil until golden.",
                        "Add the chopped tomatoes and simmer until they form a thick gravy.",
                        "Serve the pap topped with the tomato gravy."),
                ing("maize meal", 500, G), ing("water", 1000, ML), ing("salt", 5, G),
                ing("tomato", 3, PCS), ing("onion", 1, PCS), ing("oil", 30, ML)));

        recipes.add(recipe("Vetkoek", steps(
                        "Mix the flour, yeast, sugar and salt in a large bowl.",
                        "Add the lukewarm water and knead into a soft dough.",
                        "Cover and leave to rise in a warm place for one hour.",
                        "Shape into balls and deep-fry in hot oil until golden on both sides.",
                        "Drain on paper towel and serve with mince or jam."),
                ing("cake flour", 500, G), ing("instant yeast", 10, G), ing("sugar", 15, G),
                ing("salt", 5, G), ing("water", 350, ML), ing("oil", 500, ML)));

        recipes.add(recipe("Bobotie", steps(
                        "Soak the bread in the milk, then squeeze it out and keep the milk.",
                        "Fry the onion, add the mince and curry powder and brown well.",
                        "Mix in the soaked bread and chutney and spread in a baking dish.",
                        "Beat the eggs with the reserved milk and pour over the top.",
                        "Bake at 180 °C for 35 minutes until set."),
                ing("beef mince", 500, G), ing("onion", 1, PCS), ing("bread slice", 2, PCS),
                ing("milk", 250, ML), ing("egg", 2, PCS), ing("curry powder", 15, G),
                ing("chutney", 30, ML)));

        recipes.add(recipe("Samp and Beans", steps(
                        "Soak the samp and sugar beans overnight, then drain.",
                        "Cover with the water and simmer for about 2 hours until soft.",
                        "Fry the chopped onion and stir it into the pot.",
                        "Season with salt and cook until thick and creamy."),
                ing("samp", 500, G), ing("sugar beans", 250, G), ing("onion", 1, PCS),
                ing("salt", 5, G), ing("water", 2000, ML)));

        recipes.add(recipe("Egg and Tomato Scramble", steps(
                        "Fry the chopped onion in oil until soft.",
                        "Add the chopped tomatoes and cook for 3 minutes.",
                        "Beat the eggs with the salt and pour into the pan.",
                        "Stir gently over low heat until just set and serve."),
                ing("egg", 4, PCS), ing("tomato", 2, PCS), ing("onion", 1, PCS),
                ing("oil", 15, ML), ing("salt", 2, G)));

        recipes.add(recipe("Morogo and Pap", steps(
                        "Cook the maize meal in the boiling water for 25 minutes to make pap.",
                        "Fry the chopped onion in oil until soft.",
                        "Add the chopped tomato and cook until soft.",
                        "Add the washed spinach and cook until wilted.",
                        "Serve the morogo with the pap."),
                ing("spinach", 300, G), ing("onion", 1, PCS), ing("tomato", 1, PCS),
                ing("maize meal", 300, G), ing("water", 750, ML), ing("oil", 15, ML)));

        recipes.add(recipe("Boerewors Rolls", steps(
                        "Grill or pan-fry the boerewors until cooked through.",
                        "Slice and fry the onion until caramelised.",
                        "Cut the boerewors to fit the rolls.",
                        "Fill each roll with boerewors and onion and top with tomato sauce."),
                ing("boerewors", 500, G), ing("bread roll", 4, PCS), ing("onion", 1, PCS),
                ing("tomato sauce", 60, ML)));

        recipes.add(recipe("Chicken Curry", steps(
                        "Brown the chicken in oil, then set aside.",
                        "Fry the onion and crushed garlic, then add the curry powder.",
                        "Add the chopped tomatoes and cook into a sauce.",
                        "Return the chicken, add the cubed potatoes and a little water.",
                        "Simmer covered for 40 minutes until the chicken is tender."),
                ing("chicken", 1000, G), ing("onion", 1, PCS), ing("tomato", 2, PCS),
                ing("potato", 3, PCS), ing("curry powder", 20, G), ing("garlic", 3, PCS),
                ing("oil", 30, ML)));

        recipes.add(recipe("Potato Salad", steps(
                        "Boil the potatoes until tender, then cool and cube.",
                        "Hard-boil the eggs, cool, peel and chop.",
                        "Finely chop the onion.",
                        "Mix everything with the mayonnaise and chill before serving."),
                ing("potato", 6, PCS), ing("egg", 3, PCS), ing("mayonnaise", 125, ML),
                ing("onion", 1, PCS)));

        recipes.add(recipe("Pumpkin Fritters", steps(
                        "Cook the pumpkin until soft, drain well and mash.",
                        "Mix in the flour and eggs to make a thick batter.",
                        "Fry spoonfuls in hot oil until golden on both sides.",
                        "Mix the sugar and cinnamon and sprinkle over the warm fritters."),
                ing("pumpkin", 500, G), ing("cake flour", 120, G), ing("egg", 2, PCS),
                ing("sugar", 15, G), ing("cinnamon", 5, G), ing("oil", 100, ML)));

        recipes.add(recipe("French Toast", steps(
                        "Whisk the eggs, milk and sugar in a shallow dish.",
                        "Heat the oil in a frying pan.",
                        "Dip each bread slice in the egg mixture.",
                        "Fry until golden on both sides and serve warm."),
                ing("bread slice", 4, PCS), ing("egg", 2, PCS), ing("milk", 125, ML),
                ing("sugar", 10, G), ing("oil", 30, ML)));

        recipes.add(recipe("Spaghetti Bolognese", steps(
                        "Cook the spaghetti in salted boiling water and drain.",
                        "Fry the onion and garlic, then brown the mince.",
                        "Add the chopped tomatoes and simmer for 20 minutes.",
                        "Serve the sauce over the spaghetti."),
                ing("spaghetti", 500, G), ing("beef mince", 500, G), ing("onion", 1, PCS),
                ing("tomato", 4, PCS), ing("garlic", 2, PCS)));

        recipes.add(recipe("Vegetable Soup", steps(
                        "Chop the potatoes, carrots, onion and cabbage.",
                        "Place in a large pot with the water and salt.",
                        "Bring to the boil, then simmer for 30 minutes.",
                        "Blend partly for a thicker soup, or serve chunky."),
                ing("potato", 2, PCS), ing("carrot", 3, PCS), ing("onion", 1, PCS),
                ing("cabbage", 300, G), ing("water", 1500, ML), ing("salt", 5, G)));

        recipes.add(recipe("Fried Cabbage", steps(
                        "Slice the cabbage and onion thinly.",
                        "Fry the onion in oil until soft.",
                        "Add the cabbage and salt and stir-fry for 10 minutes until tender."),
                ing("cabbage", 500, G), ing("onion", 1, PCS), ing("oil", 30, ML),
                ing("salt", 3, G)));

        recipes.add(recipe("Tuna Mayo Rice", steps(
                        "Cook the rice and let it cool slightly.",
                        "Drain the tuna and flake it with a fork.",
                        "Finely chop the onion.",
                        "Mix the rice, tuna, onion and mayonnaise and serve."),
                ing("rice", 250, G), ing("tuna", 170, G), ing("onion", 1, PCS),
                ing("mayonnaise", 60, ML)));

        recipes.add(recipe("Flapjacks", steps(
                        "Sift the flour and baking powder together and add the sugar.",
                        "Whisk in the eggs and milk to make a smooth batter.",
                        "Heat a lightly oiled pan and drop in spoonfuls of batter.",
                        "Flip when bubbles appear and cook until golden."),
                ing("cake flour", 250, G), ing("milk", 250, ML), ing("egg", 2, PCS),
                ing("sugar", 30, G), ing("baking powder", 10, G), ing("oil", 15, ML)));

        recipes.add(recipe("Chicken Stew", steps(
                        "Brown the chicken pieces in a large pot.",
                        "Add the chopped onions and cook until soft.",
                        "Add the carrots, potatoes and chicken stock.",
                        "Cover and simmer for 45 minutes until tender."),
                ing("chicken", 1000, G), ing("onion", 2, PCS), ing("carrot", 2, PCS),
                ing("potato", 4, PCS), ing("chicken stock", 500, ML)));

        recipes.add(recipe("Banana Bread", steps(
                        "Preheat the oven to 180 °C and grease a loaf tin.",
                        "Cream the butter and sugar, then beat in the eggs.",
                        "Mash the bananas and stir them in.",
                        "Fold in the flour and baking powder.",
                        "Bake for 50 minutes until a skewer comes out clean."),
                ing("cake flour", 250, G), ing("banana", 3, PCS), ing("egg", 2, PCS),
                ing("sugar", 150, G), ing("baking powder", 10, G), ing("butter", 100, G)));

        recipes.add(recipe("Cheese Omelette", steps(
                        "Finely chop the onion and grate the cheese.",
                        "Whisk the eggs with the milk.",
                        "Pour into a hot pan and cook until almost set.",
                        "Sprinkle over the onion and cheese, fold and serve."),
                ing("egg", 3, PCS), ing("cheese", 50, G), ing("onion", 1, PCS),
                ing("milk", 30, ML)));
        return recipes;
    }

    /**
     * Returns the demo pantry, pre-loaded on first run and restored by "Reset to demo pantry"
     * in Settings. It is chosen so that Chakalaka, Pap and Tomato Gravy, and Egg and Tomato
     * Scramble match strictly while several other recipes are one ingredient short.
     */
    public static List<PantryItem> demoPantry() {
        return Arrays.asList(
                new PantryItem("onion", 3, PCS, null),
                new PantryItem("tomatoes", 5, PCS, null),
                new PantryItem("carrots", 2, PCS, null),
                new PantryItem("green pepper", 1, PCS, null),
                new PantryItem("baked beans", 410, G, null),
                new PantryItem("curry powder", 50, G, null),
                new PantryItem("sunflower oil", 750, ML, null),
                new PantryItem("eggs", 6, PCS, null),
                new PantryItem("salt", 500, G, null),
                new PantryItem("mealie meal", 2, KG, null),
                new PantryItem("milk", 1, L, null));
    }

    private static void insert(SQLiteDatabase db, Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_RECIPE_NAME, recipe.getName());
        values.put(DatabaseHelper.COL_RECIPE_STEPS, recipe.getSteps());
        long recipeId = db.insertOrThrow(DatabaseHelper.TABLE_RECIPES, null, values);

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ContentValues row = new ContentValues();
            row.put(DatabaseHelper.COL_RI_RECIPE_ID, recipeId);
            row.put(DatabaseHelper.COL_RI_NAME, ingredient.getName());
            row.put(DatabaseHelper.COL_RI_QUANTITY, ingredient.getQuantity());
            row.put(DatabaseHelper.COL_RI_UNIT, ingredient.getUnit());
            db.insertOrThrow(DatabaseHelper.TABLE_RECIPE_INGREDIENTS, null, row);
        }
    }

    private static Recipe recipe(String name, String steps, RecipeIngredient... ingredients) {
        return new Recipe(0, name, steps, new ArrayList<>(Arrays.asList(ingredients)));
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private static String steps(String... lines) {
        return String.join("\n", lines);
    }
}
