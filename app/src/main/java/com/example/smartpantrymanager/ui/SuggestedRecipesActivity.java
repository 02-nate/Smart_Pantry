package com.example.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.matching.RecipeMatcher;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.util.NavHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;

/**
 * Shows recipes the user can cook now (strict matches) and, in a separate section,
 * recipes that are missing exactly one ingredient. Summary tiles show both counts.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeAdapter strictAdapter;
    private RecipeAdapter almostAdapter;
    private TextView noStrictText;
    private View almostSection;
    private TextView cookNowValue;
    private TextView almostValue;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = DatabaseHelper.getInstance(this);
        noStrictText = findViewById(R.id.text_no_strict);
        almostSection = findViewById(R.id.section_almost);

        ((TextView) findViewById(R.id.tile_left_label)).setText(R.string.header_cook_now);
        ((TextView) findViewById(R.id.tile_right_label)).setText(R.string.header_almost_there);
        cookNowValue = findViewById(R.id.tile_left_value);
        almostValue = findViewById(R.id.tile_right_value);

        strictAdapter = new RecipeAdapter(false, this::openRecipe);
        almostAdapter = new RecipeAdapter(true, this::openRecipe);
        RecyclerView strictList = findViewById(R.id.recycler_strict);
        RecyclerView almostList = findViewById(R.id.recycler_almost);
        strictList.setAdapter(strictAdapter);
        almostList.setAdapter(almostAdapter);

        bottomNav = findViewById(R.id.bottom_nav);
        NavHelper.setup(this, bottomNav, R.id.nav_suggestions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavHelper.highlight(bottomNav, R.id.nav_suggestions);
        // The pantry may have changed on another screen, so re-run matching every time.
        loadSuggestions();
    }

    private void loadSuggestions() {
        RecipeMatcher.MatchResults results = RecipeMatcher.match(
                db.getAllPantryItems(), db.getAllRecipesWithIngredients(), LocalDate.now());

        cookNowValue.setText(getString(R.string.two_digit, results.getStrict().size()));
        almostValue.setText(getString(R.string.two_digit, results.getAlmostThere().size()));

        strictAdapter.submitResults(results.getStrict());
        noStrictText.setVisibility(results.getStrict().isEmpty() ? View.VISIBLE : View.GONE);

        almostAdapter.submitResults(results.getAlmostThere());
        almostSection.setVisibility(results.getAlmostThere().isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void openRecipe(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
