package com.example.smartpantrymanager.matching;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Turns free-text ingredient names and unit spellings into canonical forms so that pantry
 * items and recipe ingredients can be compared. The same rules apply to both sides.
 */
public final class IngredientNormalizer {

    private static final Map<String, String> ALIASES;
    private static final Map<String, UnitType> UNIT_SPELLINGS;

    static {
        Map<String, String> aliases = new HashMap<>();
        addAliases(aliases, "maize meal", "mealie meal", "mielie meal", "maize");
        addAliases(aliases, "beef mince", "mince", "minced beef", "ground beef");
        addAliases(aliases, "cake flour", "flour", "plain flour", "all purpose flour");
        addAliases(aliases, "oil", "cooking oil", "sunflower oil", "vegetable oil");
        addAliases(aliases, "tuna", "tinned tuna", "canned tuna");
        addAliases(aliases, "chicken", "chicken piece", "chicken breast", "chicken thigh");
        addAliases(aliases, "garlic", "garlic clove");
        addAliases(aliases, "instant yeast", "yeast", "dry yeast");
        addAliases(aliases, "sugar", "white sugar", "brown sugar");
        addAliases(aliases, "mayonnaise", "mayo");
        addAliases(aliases, "green pepper", "bell pepper", "green bell pepper");
        addAliases(aliases, "tomato sauce", "ketchup");
        addAliases(aliases, "boerewors", "wors", "sausage");
        ALIASES = Collections.unmodifiableMap(aliases);

        Map<String, UnitType> units = new HashMap<>();
        addSpellings(units, UnitType.G, "g", "gram", "grams");
        addSpellings(units, UnitType.KG, "kg", "kilogram", "kilograms");
        addSpellings(units, UnitType.ML, "ml", "millilitre", "millilitres",
                "milliliter", "milliliters");
        addSpellings(units, UnitType.L, "l", "litre", "litres", "liter", "liters");
        addSpellings(units, UnitType.TSP, "tsp", "teaspoon", "teaspoons");
        addSpellings(units, UnitType.TBSP, "tbsp", "tablespoon", "tablespoons");
        addSpellings(units, UnitType.CUP, "cup", "cups");
        addSpellings(units, UnitType.PCS, "pcs", "pc", "piece", "pieces", "each",
                "unit", "units", "whole");
        UNIT_SPELLINGS = Collections.unmodifiableMap(units);
    }

    private IngredientNormalizer() {
    }

    /**
     * Normalises an ingredient name: lower-case, letters and single spaces only,
     * last word singularised, then mapped through the alias table.
     *
     * @return the canonical name, or an empty string for null or blank input
     */
    public static String normalizeName(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.toLowerCase(Locale.ROOT).trim()
                .replaceAll("[^\\p{L} ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) {
            return "";
        }

        int lastSpace = cleaned.lastIndexOf(' ');
        String head = cleaned.substring(0, lastSpace + 1);
        String singular = head + singularize(cleaned.substring(lastSpace + 1));

        String canonical = ALIASES.get(singular);
        return canonical != null ? canonical : singular;
    }

    /**
     * Parses a unit spelling, ignoring case and surrounding spaces.
     *
     * @throws IllegalArgumentException if the spelling is not recognised
     */
    public static UnitType parseUnit(String raw) {
        UnitType unit = raw == null ? null : UNIT_SPELLINGS.get(raw.trim().toLowerCase(Locale.ROOT));
        if (unit == null) {
            throw new IllegalArgumentException("Unknown unit: " + raw);
        }
        return unit;
    }

    /** Singularises one word using a small set of English suffix rules, applied in order. */
    static String singularize(String word) {
        if (word.endsWith("ies") && word.length() > 3) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes") && word.length() > 3) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ses") || word.endsWith("xes")
                || word.endsWith("ches") || word.endsWith("shes")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("s") && word.length() > 1
                && !word.endsWith("ss") && !word.endsWith("us") && !word.endsWith("is")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    private static void addAliases(Map<String, String> map, String canonical, String... aliases) {
        for (String alias : aliases) {
            map.put(alias, canonical);
        }
    }

    private static void addSpellings(Map<String, UnitType> map, UnitType unit, String... spellings) {
        for (String spelling : spellings) {
            map.put(spelling, unit);
        }
    }
}
