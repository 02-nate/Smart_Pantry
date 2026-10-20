package com.example.smartpantrymanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.matching.RecipeMatcher;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.util.QuantityFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

/** RecyclerView adapter for pantry rows, using the ViewHolder pattern and DiffUtil. */
public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.PantryViewHolder> {

    /** Callback for taps on a pantry row. */
    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    private final OnItemClickListener listener;
    private boolean autoScaleUnits = true;

    public PantryAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
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
        holder.name.setText(item.getName());
        holder.quantity.setText(
                QuantityFormatter.format(item.getQuantity(), item.getUnit(), autoScaleUnits));

        LocalDate expiry = RecipeMatcher.parseDate(item.getExpiryDate());
        if (expiry == null) {
            holder.expiry.setVisibility(View.GONE);
        } else {
            holder.expiry.setVisibility(View.VISIBLE);
            holder.expiry.setText(holder.itemView.getContext()
                    .getString(R.string.expiry_on, DISPLAY_DATE.format(expiry)));
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    /** Holds the views of one pantry row so they are looked up only once. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;
        final TextView expiry;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name);
            quantity = itemView.findViewById(R.id.text_quantity);
            expiry = itemView.findViewById(R.id.text_expiry);
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
