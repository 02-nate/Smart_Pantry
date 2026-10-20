package com.example.smartpantrymanager.ui;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.util.NavHelper;
import com.example.smartpantrymanager.util.Prefs;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

/** User preferences plus the "Clear pantry" maintenance action. */
public class SettingsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = DatabaseHelper.getInstance(this);
        bottomNav = findViewById(R.id.bottom_nav);
        NavHelper.setup(this, bottomNav, R.id.nav_settings);

        SwitchMaterial highlightSwitch = findViewById(R.id.switch_highlight);
        highlightSwitch.setChecked(Prefs.isHighlightExpiring(this));
        highlightSwitch.setOnCheckedChangeListener((button, checked) ->
                Prefs.setHighlightExpiring(this, checked));

        RadioGroup unitsGroup = findViewById(R.id.radio_units);
        unitsGroup.check(Prefs.isAutoScaleUnits(this) ? R.id.radio_auto_scale : R.id.radio_base_units);
        unitsGroup.setOnCheckedChangeListener((group, checkedId) ->
                Prefs.setAutoScaleUnits(this, checkedId == R.id.radio_auto_scale));

        MaterialButton clearButton = findViewById(R.id.button_clear_pantry);
        clearButton.setOnClickListener(v -> confirmClear());

        TextView about = findViewById(R.id.text_about);
        about.setText(getString(R.string.about_line, getString(R.string.app_name), versionName()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavHelper.highlight(bottomNav, R.id.nav_settings);
    }

    private void confirmClear() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_clear_title)
                .setMessage(R.string.dialog_clear_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_clear, (dialog, which) -> {
                    db.clearPantry();
                    Toast.makeText(this, R.string.toast_pantry_cleared, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private String versionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Cannot happen for our own package; fall back to an empty version.
            return "";
        }
    }
}
