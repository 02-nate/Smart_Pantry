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
import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;

/**
 * Shows recipes the user can cook now (strict matches) and, in a separate section,
 * recipes that are missing exactly one ingredient.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeAdapter strictAdapter;
    private RecipeAdapter almostAdapter;
    private TextView noStrictText;
    private View almostSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = DatabaseHelper.getInstance(this);
        noStrictText = findViewById(R.id.text_no_strict);
        almostSection = findViewById(R.id.section_almost);

        strictAdapter = new RecipeAdapter(false, this::openRecipe);
        almostAdapter = new RecipeAdapter(true, this::openRecipe);
        RecyclerView strictList = findViewById(R.id.recycler_strict);
        RecyclerView almostList = findViewById(R.id.recycler_almost);
        strictList.setAdapter(strictAdapter);
        almostList.setAdapter(almostAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The pantry may have changed on another screen, so re-run matching every time.
        loadSuggestions();
    }

    private void loadSuggestions() {
        RecipeMatcher.MatchResults results = RecipeMatcher.match(
                db.getAllPantryItems(), db.getAllRecipesWithIngredients(), LocalDate.now());

        strictAdapter.submitList(results.getStrict());
        noStrictText.setVisibility(results.getStrict().isEmpty() ? View.VISIBLE : View.GONE);

        almostAdapter.submitList(results.getAlmostThere());
        almostSection.setVisibility(results.getAlmostThere().isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void openRecipe(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
