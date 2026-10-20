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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

/** Launcher screen listing every pantry item, with an empty state and an add button. */
public class PantryListActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView recycler;
    private TextView emptyText;
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
        ExtendedFloatingActionButton fab = findViewById(R.id.fab_add);

        adapter = new PantryAdapter(item -> openEditor(item.getId()));
        recycler.setAdapter(adapter);
        fab.setOnClickListener(v -> openEditor(-1));

        bottomNav = findViewById(R.id.bottom_nav);
        NavHelper.setup(this, bottomNav, R.id.nav_pantry);

        // Collapse the FAB to an icon while scrolling down so it does not hide rows.
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (dy > 0) {
                    fab.shrink();
                } else if (dy < 0) {
                    fab.extend();
                }
            }
        });
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
        List<PantryItem> items = db.getAllPantryItems();
        adapter.submitList(items);
        boolean empty = items.isEmpty();
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.INVISIBLE : View.VISIBLE);
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
