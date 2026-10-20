package com.example.smartpantrymanager.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.matching.MatchResult;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.util.CardFills;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * RecyclerView adapter for recipe suggestion tiles. The same adapter serves both the
 * "Cook now" list (numeral = ingredient count) and the "Almost there" list
 * (numeral = missing count, footer names the missing ingredient).
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

    /** Shows a new list, rebinding every row because card fills depend on position. */
    public void submitResults(List<MatchResult> results) {
        submitList(results, () -> notifyItemRangeChanged(0, getItemCount()));
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        MatchResult result = getItem(position);
        Recipe recipe = result.getRecipe();
        Context context = holder.itemView.getContext();

        CardFills.apply(holder.card, position);
        holder.name.setText(recipe.getName());
        if (showMissing && !result.getMissing().isEmpty()) {
            holder.number.setText(context.getString(R.string.two_digit, result.getMissing().size()));
            holder.footerStart.setText(R.string.label_missing);
            holder.footerEnd.setText(result.getMissing().get(0));
        } else {
            holder.number.setText(
                    context.getString(R.string.two_digit, recipe.getIngredients().size()));
            holder.footerStart.setText(R.string.label_ingredients);
            holder.footerEnd.setText(R.string.label_all_in_pantry);
        }
        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    /** Holds the views of one recipe tile. */
    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView name;
        final TextView number;
        final TextView footerStart;
        final TextView footerEnd;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            name = itemView.findViewById(R.id.text_recipe_name);
            number = itemView.findViewById(R.id.text_recipe_number);
            footerStart = itemView.findViewById(R.id.text_footer_start);
            footerEnd = itemView.findViewById(R.id.text_footer_end);
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
