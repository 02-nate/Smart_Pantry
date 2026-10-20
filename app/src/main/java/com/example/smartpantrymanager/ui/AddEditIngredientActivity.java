package com.example.smartpantrymanager.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.matching.RecipeMatcher;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.util.QuantityFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Form for adding a new pantry item or, when {@link #EXTRA_ITEM_ID} is supplied,
 * editing or deleting an existing one.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Intent extra (long): id of the pantry item to edit. Absent means add mode. */
    public static final String EXTRA_ITEM_ID = "com.example.smartpantrymanager.EXTRA_ITEM_ID";

    private static final String STATE_EXPIRY = "state_expiry";
    private static final double MAX_QUANTITY = 100000;
    private static final Pattern NAME_PATTERN = Pattern.compile("[\\p{L} -]+");
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    private DatabaseHelper db;
    private PantryItem editingItem;
    private List<String> unitOptions;
    /** Selected expiry date in ISO format, or null for none. */
    private String expiryDate;

    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputLayout unitLayout;
    private TextInputEditText nameInput;
    private TextInputEditText quantityInput;
    private MaterialAutoCompleteTextView unitInput;
    private TextInputEditText expiryInput;
    private MaterialButton clearDateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = DatabaseHelper.getInstance(this);
        unitOptions = Arrays.asList(getResources().getStringArray(R.array.units));

        nameLayout = findViewById(R.id.layout_name);
        quantityLayout = findViewById(R.id.layout_quantity);
        unitLayout = findViewById(R.id.layout_unit);
        nameInput = findViewById(R.id.edit_name);
        quantityInput = findViewById(R.id.edit_quantity);
        unitInput = findViewById(R.id.edit_unit);
        expiryInput = findViewById(R.id.edit_expiry);
        clearDateButton = findViewById(R.id.button_clear_date);
        MaterialButton saveButton = findViewById(R.id.button_save);
        MaterialButton deleteButton = findViewById(R.id.button_delete);

        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            editingItem = db.getPantryItem(itemId);
            if (editingItem == null) {
                Toast.makeText(this, R.string.toast_item_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        boolean editMode = editingItem != null;
        setTitle(editMode ? R.string.title_edit_ingredient : R.string.title_add_ingredient);
        TextView display = findViewById(R.id.text_display);
        if (editMode) {
            display.setText(editingItem.getName());
        } else {
            display.setText(R.string.display_add_ingredient);
        }
        deleteButton.setVisibility(editMode ? View.VISIBLE : View.GONE);

        if (savedInstanceState != null) {
            // Text fields restore themselves; only the date lives outside a view.
            expiryDate = savedInstanceState.getString(STATE_EXPIRY);
        } else if (editMode) {
            prefill(editingItem);
        }
        showExpiryDate();

        expiryInput.setOnClickListener(v -> showDatePicker());
        ((TextInputLayout) findViewById(R.id.layout_expiry))
                .setStartIconOnClickListener(v -> showDatePicker());
        clearDateButton.setOnClickListener(v -> {
            expiryDate = null;
            showExpiryDate();
        });
        unitInput.setOnItemClickListener((parent, view, position, id) -> unitLayout.setError(null));
        saveButton.setOnClickListener(v -> save());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPIRY, expiryDate);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void prefill(PantryItem item) {
        nameInput.setText(item.getName());
        quantityInput.setText(QuantityFormatter.formatNumber(item.getQuantity()));
        unitInput.setText(item.getUnit(), false);
        expiryDate = item.getExpiryDate();
    }

    private void showExpiryDate() {
        LocalDate date = RecipeMatcher.parseDate(expiryDate);
        expiryInput.setText(date == null ? "" : DISPLAY_DATE.format(date));
        clearDateButton.setEnabled(date != null);
    }

    private void showDatePicker() {
        LocalDate initial = RecipeMatcher.parseDate(expiryDate);
        if (initial == null) {
            initial = LocalDate.now();
        }
        // DatePickerDialog months are zero-based; LocalDate months are one-based.
        new DatePickerDialog(this, (picker, year, month, day) -> {
            expiryDate = LocalDate.of(year, month + 1, day).toString();
            showExpiryDate();
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth()).show();
    }

    private void save() {
        String name = validateName();
        Double quantity = validateQuantity();
        String unit = validateUnit();
        if (name == null || quantity == null || unit == null) {
            return;
        }

        boolean saved;
        if (editingItem == null) {
            saved = db.insertPantryItem(new PantryItem(name, quantity, unit, expiryDate)) != -1;
        } else {
            editingItem.setName(name);
            editingItem.setQuantity(quantity);
            editingItem.setUnit(unit);
            editingItem.setExpiryDate(expiryDate);
            saved = db.updatePantryItem(editingItem) > 0;
        }

        if (saved) {
            Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /** Returns the trimmed name, or null after showing an error. */
    private String validateName() {
        String name = textOf(nameInput.getText());
        int min = getResources().getInteger(R.integer.name_min_length);
        int max = getResources().getInteger(R.integer.name_max_length);
        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_required));
            return null;
        }
        if (name.length() < min || name.length() > max) {
            nameLayout.setError(getString(R.string.error_name_length, min, max));
            return null;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            nameLayout.setError(getString(R.string.error_name_chars));
            return null;
        }
        nameLayout.setError(null);
        return name;
    }

    /** Returns the quantity, or null after showing an error. */
    private Double validateQuantity() {
        String text = textOf(quantityInput.getText()).replace(',', '.');
        if (text.isEmpty()) {
            quantityLayout.setError(getString(R.string.error_quantity_required));
            return null;
        }
        double value;
        try {
            value = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            quantityLayout.setError(getString(R.string.error_quantity_invalid));
            return null;
        }
        if (Double.isNaN(value) || value <= 0 || value > MAX_QUANTITY) {
            quantityLayout.setError(getString(R.string.error_quantity_range));
            return null;
        }
        quantityLayout.setError(null);
        return value;
    }

    /** Returns the selected unit, or null after showing an error. */
    private String validateUnit() {
        String unit = textOf(unitInput.getText());
        if (!unitOptions.contains(unit)) {
            unitLayout.setError(getString(R.string.error_unit_required));
            return null;
        }
        unitLayout.setError(null);
        return unit;
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, editingItem.getName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    db.deletePantryItem(editingItem.getId());
                    Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    private static String textOf(Editable editable) {
        return editable == null ? "" : editable.toString().trim();
    }
}
