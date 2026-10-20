package com.example.smartpantrymanager.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Typed access to the app's SharedPreferences. */
public final class Prefs {

    private static final String FILE_NAME = "smart_pantry_prefs";

    public static final String KEY_HIGHLIGHT_EXPIRING = "highlight_expiring";
    public static final String KEY_AUTO_SCALE_UNITS = "auto_scale_units";

    private static final boolean DEFAULT_HIGHLIGHT_EXPIRING = true;
    private static final boolean DEFAULT_AUTO_SCALE_UNITS = true;

    private Prefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /** Whether expiring and expired pantry items are highlighted. Default: on. */
    public static boolean isHighlightExpiring(Context context) {
        return prefs(context).getBoolean(KEY_HIGHLIGHT_EXPIRING, DEFAULT_HIGHLIGHT_EXPIRING);
    }

    public static void setHighlightExpiring(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_HIGHLIGHT_EXPIRING, enabled).apply();
    }

    /** Whether large amounts are shown in kg / L (true) or always in g / ml (false). Default: on. */
    public static boolean isAutoScaleUnits(Context context) {
        return prefs(context).getBoolean(KEY_AUTO_SCALE_UNITS, DEFAULT_AUTO_SCALE_UNITS);
    }

    public static void setAutoScaleUnits(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_AUTO_SCALE_UNITS, enabled).apply();
    }
}
