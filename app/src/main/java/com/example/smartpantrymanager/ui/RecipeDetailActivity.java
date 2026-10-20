package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.matching.RecipeMatcher;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;
import com.example.smartpantrymanager.util.QuantityFormatter;
import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;
import java.util.List;

/**
 * Shows one recipe: its ingredients with an in-pantry status and its numbered method.
 * The status uses {@link RecipeMatcher.PantryStock} so it always agrees with the suggestions.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Intent extra (long): id of the recipe to show. */
    public static final String EXTRA_RECIPE_ID = "com.example.smartpantrymanager.EXTRA_RECIPE_ID";

    private long recipeId;
    private TextView titleText;
    private LinearLayout ingredientsContainer;
    private LinearLayout stepsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        titleText = findViewById(R.id.text_recipe_title);
        ingredientsContainer = findViewById(R.id.container_ingredients);
        stepsContainer = findViewById(R.id.container_steps);
        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        Recipe recipe = recipeId < 0 ? null : db.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, R.string.toast_recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bind(recipe, RecipeMatcher.buildStock(db.getAllPantryItems(), LocalDate.now()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void bind(Recipe recipe, RecipeMatcher.PantryStock stock) {
        setTitle(recipe.getName());
        titleText.setText(recipe.getName());

        LayoutInflater inflater = LayoutInflater.from(this);
        ingredientsContainer.removeAllViews();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            View row = inflater.inflate(R.layout.item_recipe_ingredient, ingredientsContainer, false);
            TextView status = row.findViewById(R.id.text_status);
            TextView line = row.findViewById(R.id.text_ingredient);

            boolean available = stock.covers(ingredient);
            status.setText(available ? R.string.status_available : R.string.status_missing);
            status.setTextColor(ContextCompat.getColor(this,
                    available ? R.color.status_available : R.color.status_missing));
            status.setContentDescription(getString(
                    available ? R.string.cd_status_available : R.string.cd_status_missing));
            // Recipe amounts are shown exactly as written, never auto-scaled.
            line.setText(getString(R.string.ingredient_line,
                    QuantityFormatter.format(ingredient.getQuantity(), ingredient.getUnit(), false),
                    ingredient.getName()));
            ingredientsContainer.addView(row);
        }

        stepsContainer.removeAllViews();
        List<String> steps = recipe.getStepList();
        int stepSpacing = getResources().getDimensionPixelSize(R.dimen.step_spacing);
        for (int i = 0; i < steps.size(); i++) {
            TextView step = new TextView(this);
            step.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            step.setText(getString(R.string.step_line, i + 1, steps.get(i)));
            step.setPadding(0, 0, 0, stepSpacing);
            stepsContainer.addView(step);
        }
    }
}
