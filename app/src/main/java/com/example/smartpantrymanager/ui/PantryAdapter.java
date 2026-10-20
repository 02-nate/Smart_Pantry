package com.example.smartpantrymanager.ui;

import android.content.Context;
import android.content.res.ColorStateList;
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
import com.example.smartpantrymanager.matching.RecipeMatcher;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.util.QuantityFormatter;
import com.google.android.material.card.MaterialCardView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

/**
 * RecyclerView adapter for pantry rows, using the ViewHolder pattern and DiffUtil.
 * When highlighting is on, items expiring within {@link #EXPIRING_SOON_DAYS} days get an amber
 * card and expired items get red text, matching the matcher's rule that expired items are unused.
 */
public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.PantryViewHolder> {

    /** Callback for taps on a pantry row. */
    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    /** Items expiring between today and this many days from now count as "expiring soon". */
    public static final int EXPIRING_SOON_DAYS = 3;

    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    private final OnItemClickListener listener;
    private boolean autoScaleUnits = true;
    private boolean highlightExpiring = true;

    public PantryAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /** Switches between g / ml only and auto-scaled kg / L, rebinding rows if it changed. */
    public void setAutoScaleUnits(boolean autoScale) {
        if (autoScale != autoScaleUnits) {
            autoScaleUnits = autoScale;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    /** Turns expiry highlighting on or off, rebinding rows if it changed. */
    public void setHighlightExpiring(boolean highlight) {
        if (highlight != highlightExpiring) {
            highlightExpiring = highlight;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = getItem(position);
        Context context = holder.itemView.getContext();

        holder.name.setText(item.getName());
        holder.quantity.setText(
                QuantityFormatter.format(item.getQuantity(), item.getUnit(), autoScaleUnits));

        // Rows are recycled, so every highlight is reset before it is (maybe) reapplied.
        holder.resetHighlight();

        LocalDate expiry = RecipeMatcher.parseDate(item.getExpiryDate());
        if (expiry == null) {
            holder.expiry.setVisibility(View.GONE);
        } else {
            holder.expiry.setVisibility(View.VISIBLE);
            holder.expiry.setText(context.getString(R.string.expiry_on, DISPLAY_DATE.format(expiry)));
            if (highlightExpiring) {
                applyHighlight(holder, context, expiry, LocalDate.now());
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    private static void applyHighlight(PantryViewHolder holder, Context context,
                                       LocalDate expiry, LocalDate today) {
        if (expiry.isBefore(today)) {
            int red = ContextCompat.getColor(context, R.color.expired_text);
            holder.expiry.setTextColor(red);
            holder.status.setTextColor(red);
            holder.status.setText(R.string.label_expired);
            holder.status.setVisibility(View.VISIBLE);
        } else if (!expiry.isAfter(today.plusDays(EXPIRING_SOON_DAYS))) {
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.expiring_soon_background));
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.expiring_soon_text));
            holder.status.setText(R.string.label_expiring_soon);
            holder.status.setVisibility(View.VISIBLE);
        }
    }

    /** Holds the views of one pantry row so they are looked up only once. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final TextView status;
        private final ColorStateList defaultCardColor;
        private final ColorStateList defaultExpiryColor;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            name = itemView.findViewById(R.id.text_name);
            quantity = itemView.findViewById(R.id.text_quantity);
            expiry = itemView.findViewById(R.id.text_expiry);
            status = itemView.findViewById(R.id.text_expiry_status);
            defaultCardColor = card.getCardBackgroundColor();
            defaultExpiryColor = expiry.getTextColors();
        }

        void resetHighlight() {
            card.setCardBackgroundColor(defaultCardColor);
            expiry.setTextColor(defaultExpiryColor);
            status.setVisibility(View.GONE);
        }
    }

    private static final DiffUtil.ItemCallback<PantryItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PantryItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull PantryItem a, @NonNull PantryItem b) {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull PantryItem a, @NonNull PantryItem b) {
                    return Objects.equals(a.getName(), b.getName())
                            && a.getQuantity() == b.getQuantity()
                            && Objects.equals(a.getUnit(), b.getUnit())
                            && Objects.equals(a.getExpiryDate(), b.getExpiryDate());
                }
            };
}
