package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

/** Launcher screen listing every pantry item, with an empty state and an add button. */
public class PantryListActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView recycler;
    private TextView emptyText;

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

        adapter = new PantryAdapter(item -> { });
        recycler.setAdapter(adapter);

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
        loadPantry();
    }

    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        adapter.submitList(items);
        boolean empty = items.isEmpty();
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.INVISIBLE : View.VISIBLE);
    }
}
