package com.example.smartpantrymanager.util;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.smartpantrymanager.R;
import com.google.android.material.card.MaterialCardView;

/**
 * Applies the design's card fill rotation (lime, white, lavender, mint) by list position,
 * so no two adjacent cards share a fill. White cards get a hairline so they still read as tiles.
 */
public final class CardFills {

    private static final int[] FILLS = {
            R.color.bg_green, R.color.bg_white, R.color.bg_lavender, R.color.bg_mint
    };

    private CardFills() {
    }

    /** Colours {@code card} for its position in a list. */
    public static void apply(MaterialCardView card, int position) {
        Context context = card.getContext();
        int fill = FILLS[position % FILLS.length];
        card.setCardBackgroundColor(ContextCompat.getColor(context, fill));
        card.setStrokeWidth(fill == R.color.bg_white
                ? context.getResources().getDimensionPixelSize(R.dimen.hairline_stroke)
                : 0);
    }
}
