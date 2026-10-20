package com.example.smartpantrymanager.matching;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Matches recipes against the pantry using a strict rule: a recipe is suggested only when
 * every ingredient is present in at least the required quantity. Recipes missing exactly one
 * ingredient are reported separately in an "Almost There" list.
 *
 * <p>Deliberate simplifications:
 * <ul>
 *   <li>Mass and volume are treated as interchangeable using <b>1 g &asymp; 1 ml</b>. Densities
 *       differ per ingredient, but this keeps "500 ml milk" and "500 g milk" comparable without
 *       a per-ingredient density table. Count units are only ever compared with count units.</li>
 *   <li>{@link #STAPLES} (water) are always treated as available because tap water is not
 *       something users track as a pantry item.</li>
 *   <li>Items whose expiry date is before {@code today} are ignored; an item expiring today
 *       can still be used.</li>
 * </ul>
 *
 * <p>This class has no Android dependencies so it can be unit-tested with plain JUnit.
 */
public final class RecipeMatcher {

    /** Ingredients that are always considered available (normalised names). */
    public static final Set<String> STAPLES = Collections.singleton("water");

    /** Tolerance for floating-point rounding when comparing quantities. */
    private static final double EPSILON = 1e-6;

    private static final Comparator<MatchResult> BY_NAME = (a, b) ->
            String.CASE_INSENSITIVE_ORDER.compare(a.getRecipe().getName(), b.getRecipe().getName());

    private RecipeMatcher() {
    }

    /**
     * Classifies recipes into strict matches and "Almost There" matches.
     *
     * @param pantry  the user's pantry items
     * @param recipes the recipes to check
     * @param today   the date used to decide which items have expired
     * @return both lists, sorted by recipe name
     */
    public static MatchResults match(List<PantryItem> pantry, List<Recipe> recipes, LocalDate today) {
        PantryStock stock = buildStock(pantry, today);
        List<MatchResult> strict = new ArrayList<>();
        List<MatchResult> almostThere = new ArrayList<>();

        for (Recipe recipe : recipes) {
            if (recipe.getIngredients().isEmpty()) {
                continue;
            }
            List<String> missing = new ArrayList<>();
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                if (!stock.covers(ingredient)) {
                    missing.add(ingredient.getName());
                }
            }
            if (missing.isEmpty()) {
                strict.add(new MatchResult(recipe, missing));
            } else if (missing.size() == 1) {
                almostThere.add(new MatchResult(recipe, missing));
            }
        }

        strict.sort(BY_NAME);
        almostThere.sort(BY_NAME);
        return new MatchResults(strict, almostThere);
    }

    /**
     * Totals the usable (non-expired) pantry quantities per normalised name and dimension.
     * Exposed so the recipe detail screen can show per-ingredient status with the same rules.
     */
    public static PantryStock buildStock(List<PantryItem> pantry, LocalDate today) {
        PantryStock stock = new PantryStock();
        for (PantryItem item : pantry) {
            if (isExpired(item, today)) {
                continue;
            }
            UnitType unit;
            try {
                unit = IngredientNormalizer.parseUnit(item.getUnit());
            } catch (IllegalArgumentException e) {
                // A corrupt row should not break suggestions for the rest of the pantry.
                continue;
            }
            stock.add(IngredientNormalizer.normalizeName(item.getName()),
                    unit.getDimension(), unit.toBase(item.getQuantity()));
        }
        return stock;
    }

    /** Returns true when the item has an expiry date strictly before {@code today}. */
    public static boolean isExpired(PantryItem item, LocalDate today) {
        LocalDate expiry = parseDate(item.getExpiryDate());
        return expiry != null && expiry.isBefore(today);
    }

    /** Parses an ISO date, returning null for null, blank or malformed input. */
    public static LocalDate parseDate(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(isoDate.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Aggregated pantry quantities in base units, keyed by normalised ingredient name. */
    public static final class PantryStock {

        private final Map<String, EnumMap<Dimension, Double>> totals = new HashMap<>();

        void add(String name, Dimension dimension, double baseAmount) {
            EnumMap<Dimension, Double> byDimension = totals.get(name);
            if (byDimension == null) {
                byDimension = new EnumMap<>(Dimension.class);
                totals.put(name, byDimension);
            }
            Double current = byDimension.get(dimension);
            byDimension.put(dimension, (current == null ? 0 : current) + baseAmount);
        }

        /**
         * Returns the amount available for a requirement of the given dimension.
         * Mass and volume are pooled (1 g &asymp; 1 ml); count stands alone.
         */
        public double available(String normalizedName, Dimension dimension) {
            EnumMap<Dimension, Double> byDimension = totals.get(normalizedName);
            if (byDimension == null) {
                return 0;
            }
            if (dimension == Dimension.COUNT) {
                return valueOf(byDimension, Dimension.COUNT);
            }
            return valueOf(byDimension, Dimension.MASS) + valueOf(byDimension, Dimension.VOLUME);
        }

        /** Returns true when the pantry holds enough of this ingredient, or it is a staple. */
        public boolean covers(RecipeIngredient ingredient) {
            String name = IngredientNormalizer.normalizeName(ingredient.getName());
            if (STAPLES.contains(name)) {
                return true;
            }
            UnitType unit;
            try {
                unit = IngredientNormalizer.parseUnit(ingredient.getUnit());
            } catch (IllegalArgumentException e) {
                return false;
            }
            double required = unit.toBase(ingredient.getQuantity());
            return available(name, unit.getDimension()) + EPSILON >= required;
        }

        private static double valueOf(Map<Dimension, Double> map, Dimension dimension) {
            Double value = map.get(dimension);
            return value == null ? 0 : value;
        }
    }

    /** The two output lists of {@link #match}. */
    public static final class MatchResults {

        private final List<MatchResult> strict;
        private final List<MatchResult> almostThere;

        public MatchResults(List<MatchResult> strict, List<MatchResult> almostThere) {
            this.strict = Collections.unmodifiableList(strict);
            this.almostThere = Collections.unmodifiableList(almostThere);
        }

        /** Recipes with every ingredient available. */
        public List<MatchResult> getStrict() {
            return strict;
        }

        /** Recipes missing exactly one ingredient. */
        public List<MatchResult> getAlmostThere() {
            return almostThere;
        }
    }
}
