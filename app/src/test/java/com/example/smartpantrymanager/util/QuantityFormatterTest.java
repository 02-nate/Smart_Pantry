package com.example.smartpantrymanager.util;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

/** Unit tests for {@link QuantityFormatter}. */
public class QuantityFormatterTest {

    private Locale originalLocale;

    @Before
    public void fixLocale() {
        // Decimal separators depend on the default locale, so pin it for stable assertions.
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @After
    public void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    @Test
    public void autoScale_showsKilogramsAtOrAboveThreshold() {
        assertEquals("2 kg", QuantityFormatter.format(2000, "g", true));
        assertEquals("1.5 L", QuantityFormatter.format(1500, "ml", true));
    }

    @Test
    public void autoScale_keepsSmallAmountsInBaseUnits() {
        assertEquals("750 ml", QuantityFormatter.format(750, "ml", true));
    }

    @Test
    public void baseUnits_alwaysShowsGramsAndMillilitres() {
        assertEquals("2000 g", QuantityFormatter.format(2, "kg", false));
        assertEquals("15 ml", QuantityFormatter.format(1, "tbsp", false));
    }

    @Test
    public void countUnits_areNeverConverted() {
        assertEquals("3 pcs", QuantityFormatter.format(3, "pcs", true));
    }
}
