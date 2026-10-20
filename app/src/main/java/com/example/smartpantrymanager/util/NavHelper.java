package com.example.smartpantrymanager.util;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.IdRes;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.ui.PantryListActivity;
import com.example.smartpantrymanager.ui.SettingsActivity;
import com.example.smartpantrymanager.ui.SuggestedRecipesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Wires the shared BottomNavigationView between the three top-level screens.
 * Tabs are separate Activities; switching reorders an existing instance to the front
 * instead of stacking duplicates, and suppresses the transition so it feels like a tab change.
 */
public final class NavHelper {

    private NavHelper() {
    }

    /** Highlights {@code currentItemId} and starts the matching Activity when another tab is tapped. */
    public static void setup(Activity activity, BottomNavigationView nav, @IdRes int currentItemId) {
        highlight(nav, currentItemId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentItemId) {
                return true;
            }
            Class<? extends Activity> target = targetFor(id);
            if (target == null) {
                return false;
            }
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(intent);
            disableTransition(activity);
            // Leave the highlight on this screen's tab; it shows if the user comes back here.
            return false;
        });
    }

    /** Marks the current tab as selected; call from onResume after a reorder-to-front. */
    public static void highlight(BottomNavigationView nav, @IdRes int currentItemId) {
        nav.getMenu().findItem(currentItemId).setChecked(true);
    }

    private static Class<? extends Activity> targetFor(@IdRes int itemId) {
        if (itemId == R.id.nav_pantry) {
            return PantryListActivity.class;
        } else if (itemId == R.id.nav_suggestions) {
            return SuggestedRecipesActivity.class;
        } else if (itemId == R.id.nav_settings) {
            return SettingsActivity.class;
        }
        return null;
    }

    // Still honoured on current Android versions; FLAG_ACTIVITY_NO_ANIMATION covers the rest.
    @SuppressWarnings("deprecation")
    private static void disableTransition(Activity activity) {
        activity.overridePendingTransition(0, 0);
    }
}
