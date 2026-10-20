package com.example.smartpantrymanager.matching;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Unit tests for {@link IngredientNormalizer}. */
public class IngredientNormalizerTest {

    private static final double DELTA = 1e-9;

    @Test
    public void normalizeName_oesPluralBecomesSingular() {
        assertEquals("tomato", IngredientNormalizer.normalizeName("tomatoes"));
        assertEquals("potato", IngredientNormalizer.normalizeName("potatoes"));
    }

    @Test
    public void normalizeName_iesPluralBecomesY() {
        assertEquals("berry", IngredientNormalizer.normalizeName("berries"));
    }

    @Test
    public void normalizeName_trimsAndLowercases() {
        assertEquals("onion", IngredientNormalizer.normalizeName(" Onions "));
    }

    @Test
    public void normalizeName_singularisesLastWordOnly() {
        assertEquals("bread roll", IngredientNormalizer.normalizeName("bread rolls"));
    }

    @Test
    public void normalizeName_appliesAliasMap() {
        assertEquals("maize meal", IngredientNormalizer.normalizeName("mealie meal"));
        assertEquals("oil", IngredientNormalizer.normalizeName("Sunflower Oil"));
    }

    @Test
    public void normalizeName_leavesUsEndingUnchanged() {
        assertEquals("hummus", IngredientNormalizer.normalizeName("hummus"));
    }

    @Test
    public void parseUnit_kgHasFactor1000() {
        UnitType unit = IngredientNormalizer.parseUnit("KG");
        assertEquals(UnitType.KG, unit);
        assertEquals(1000, unit.getFactor(), DELTA);
    }

    @Test
    public void parseUnit_tbspIsVolume15() {
        UnitType unit = IngredientNormalizer.parseUnit("tbsp");
        assertEquals(Dimension.VOLUME, unit.getDimension());
        assertEquals(15, unit.getFactor(), DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseUnit_unknownUnitThrows() {
        IngredientNormalizer.parseUnit("handful");
    }
}
