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
import com.example.smartpantrymanager.util.CardFills;
import com.example.smartpantrymanager.util.QuantityFormatter;
import com.google.android.material.card.MaterialCardView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * RecyclerView adapter for pantry tiles, using the ViewHolder pattern and DiffUtil.
 * Each tile shows the days left until expiry as a large zero-padded numeral. When highlighting
 * is on, items within {@link #EXPIRING_SOON_DAYS} days are labelled "Expires soon", expired
 * items are labelled as unused for suggestions, and the numeral turns red at
 * {@link #URGENT_DAYS} days or fewer.
 */
public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.PantryViewHolder> {

    /** Callback for taps on a pantry row. */
    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    /** Items expiring between today and this many days from now count as "expiring soon". */
    public static final int EXPIRING_SOON_DAYS = 3;

    /** At or below this many days left, the days numeral is shown in red. */
    public static final int URGENT_DAYS = 2;

    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    private final OnItemClickListener listener;
    private boolean autoScaleUnits = true;
    private boolean highlightExpiring = true;

    public PantryAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /**
     * Shows a new list. Card fills depend on position, so every row is rebound once the
     * diff is applied; otherwise rows that only moved would keep their old fill.
     */
    public void submitItems(List<PantryItem> items) {
        submitList(items, () -> notifyItemRangeChanged(0, getItemCount()));
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

    /** Returns true when the item has not expired but expires within {@link #EXPIRING_SOON_DAYS}. */
    public static boolean isExpiringSoon(PantryItem item, LocalDate today) {
        LocalDate expiry = RecipeMatcher.parseDate(item.getExpiryDate());
        return expiry != null && !expiry.isBefore(today)
                && !expiry.isAfter(today.plusDays(EXPIRING_SOON_DAYS));
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

        CardFills.apply(holder.card, position);
        holder.name.setText(item.getName());
        holder.quantity.setText(
                QuantityFormatter.format(item.getQuantity(), item.getUnit(), autoScaleUnits));
        // Rows are recycled, so the urgent colour is reset before it is (maybe) reapplied.
        holder.days.setTextColor(holder.defaultDaysColor);

        LocalDate expiry = RecipeMatcher.parseDate(item.getExpiryDate());
        if (expiry == null) {
            holder.days.setText(R.string.no_value);
            holder.expiryLabel.setText(R.string.label_no_expiry);
        } else {
            bindExpiry(holder, context, expiry, LocalDate.now());
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    private void bindExpiry(PantryViewHolder holder, Context context,
                            LocalDate expiry, LocalDate today) {
        long daysLeft = Math.max(0, ChronoUnit.DAYS.between(today, expiry));
        String date = DISPLAY_DATE.format(expiry);
        holder.days.setText(context.getString(R.string.two_digit, daysLeft));

        boolean expired = expiry.isBefore(today);
        if (expired) {
            holder.expiryLabel.setText(highlightExpiring
                    ? context.getString(R.string.label_expired)
                    : context.getString(R.string.label_expired_on, date));
        } else if (highlightExpiring && daysLeft <= EXPIRING_SOON_DAYS) {
            holder.expiryLabel.setText(context.getString(R.string.label_expiring_soon, date));
        } else {
            holder.expiryLabel.setText(context.getString(R.string.label_days_left, date));
        }

        if (highlightExpiring && daysLeft <= URGENT_DAYS) {
            holder.days.setTextColor(ContextCompat.getColor(context, R.color.urgent));
        }
    }

    /** Holds the views of one pantry tile so they are looked up only once. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView name;
        final TextView days;
        final TextView expiryLabel;
        final TextView quantity;
        final ColorStateList defaultDaysColor;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            name = itemView.findViewById(R.id.text_name);
            days = itemView.findViewById(R.id.text_days);
            expiryLabel = itemView.findViewById(R.id.text_expiry_label);
            quantity = itemView.findViewById(R.id.text_quantity);
            defaultDaysColor = days.getTextColors();
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
