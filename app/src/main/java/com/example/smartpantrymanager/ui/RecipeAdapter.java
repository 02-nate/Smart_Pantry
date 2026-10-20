package com.example.smartpantrymanager.ui;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.matching.MatchResult;
import com.example.smartpantrymanager.model.Recipe;
import com.google.android.material.card.MaterialCardView;

/**
 * RecyclerView adapter for recipe suggestion cards. The same adapter serves both the
 * "Cook now" list and the "Almost there" list; only the summary line differs.
 */
public class RecipeAdapter extends ListAdapter<MatchResult, RecipeAdapter.RecipeViewHolder> {

    /** Callback for taps on a recipe card. */
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final boolean showMissing;
    private final OnRecipeClickListener listener;

    /**
     * @param showMissing true for the "Almost there" list, which shows the missing ingredient
     */
    public RecipeAdapter(boolean showMissing, OnRecipeClickListener listener) {
        super(DIFF_CALLBACK);
        this.showMissing = showMissing;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        // Each list has its own tint so "Cook now" and "Almost there" read as separate sections.
        ((MaterialCardView) view).setCardBackgroundColor(ContextCompat.getColor(
                parent.getContext(),
                showMissing ? R.color.almost_there_background : R.color.cook_now_background));
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        MatchResult result = getItem(position);
        Recipe recipe = result.getRecipe();
        Resources res = holder.itemView.getResources();

        holder.name.setText(recipe.getName());
        if (showMissing && !result.getMissing().isEmpty()) {
            holder.summary.setText(res.getString(R.string.missing_ingredient,
                    result.getMissing().get(0)));
        } else {
            int count = recipe.getIngredients().size();
            holder.summary.setText(res.getQuantityString(R.plurals.recipe_all_available,
                    count, count));
        }
        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    /** Holds the views of one recipe card. */
    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView summary;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_recipe_name);
            summary = itemView.findViewById(R.id.text_recipe_summary);
        }
    }

    private static final DiffUtil.ItemCallback<MatchResult> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MatchResult>() {
                @Override
                public boolean areItemsTheSame(@NonNull MatchResult a, @NonNull MatchResult b) {
                    return a.getRecipe().getId() == b.getRecipe().getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull MatchResult a, @NonNull MatchResult b) {
                    return a.getMissing().equals(b.getMissing());
                }
            };
}
