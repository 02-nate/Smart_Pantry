package com.example.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.util.NavHelper;
import com.example.smartpantrymanager.util.Prefs;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.List;

/**
 * Launcher screen: masthead, summary tiles (total and expiring-soon counts), the pantry
 * tiles, an empty state and an add button.
 */
public class PantryListActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView recycler;
    private TextView emptyText;
    private TextView totalValue;
    private TextView expiringValue;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = DatabaseHelper.getInstance(this);
        recycler = findViewById(R.id.recycler_pantry);
        emptyText = findViewById(R.id.text_empty);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        ((TextView) findViewById(R.id.tile_left_label)).setText(R.string.tile_total_items);
        ((TextView) findViewById(R.id.tile_right_label)).setText(R.string.tile_expiring_soon);
        totalValue = findViewById(R.id.tile_left_value);
        expiringValue = findViewById(R.id.tile_right_value);

        adapter = new PantryAdapter(item -> openEditor(item.getId()));
        recycler.setAdapter(adapter);
        fab.setOnClickListener(v -> openEditor(-1));

        bottomNav = findViewById(R.id.bottom_nav);
        NavHelper.setup(this, bottomNav, R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavHelper.highlight(bottomNav, R.id.nav_pantry);
        loadPantry();
    }

    private void loadPantry() {
        // Settings may have changed while this screen was in the background.
        adapter.setAutoScaleUnits(Prefs.isAutoScaleUnits(this));
        adapter.setHighlightExpiring(Prefs.isHighlightExpiring(this));
        List<PantryItem> items = db.getAllPantryItems();
        adapter.submitItems(items);

        LocalDate today = LocalDate.now();
        int expiringSoon = 0;
        for (PantryItem item : items) {
            if (PantryAdapter.isExpiringSoon(item, today)) {
                expiringSoon++;
            }
        }
        totalValue.setText(getString(R.string.two_digit, items.size()));
        expiringValue.setText(getString(R.string.two_digit, expiringSoon));

        boolean empty = items.isEmpty();
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    /** Opens the add/edit form; a negative id opens it in add mode. */
    private void openEditor(long itemId) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        if (itemId >= 0) {
            intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, itemId);
        }
        startActivity(intent);
    }
}
