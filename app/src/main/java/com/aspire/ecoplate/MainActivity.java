package com.aspire.ecoplate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_MIN_MILLIS = 900;

    private DatabaseHelper db;
    private PantryAdapter adapter;

    private TextView tvHeadline;
    private TextView tvTally;
    private TextView tvListNote;
    private View emptyState;
    private RecyclerView rvPantry;
    private MaterialButton btnRecipes;

    private boolean keepSplash = true;

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Must run before super.onCreate. Swaps the launch theme to Theme.EcoPlate afterwards.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Hold the splash briefly so the leaf is actually seen.
        splashScreen.setKeepOnScreenCondition(() -> keepSplash);
        new Handler(Looper.getMainLooper()).postDelayed(() -> keepSplash = false, SPLASH_MIN_MILLIS);

        setContentView(R.layout.activity_main);
        Ui.edgeToEdge(this, findViewById(R.id.root));

        db = new DatabaseHelper(this);

        tvHeadline = findViewById(R.id.tvHeadline);
        tvTally = findViewById(R.id.tvTally);
        tvListNote = findViewById(R.id.tvListNote);
        emptyState = findViewById(R.id.emptyState);
        rvPantry = findViewById(R.id.rvPantry);
        btnRecipes = findViewById(R.id.btnRecipes);
        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        adapter = new PantryAdapter(this::showItemActions);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        rvPantry.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddItemActivity.class)));
        btnRecipes.setOnClickListener(v -> startActivity(new Intent(this, RecipeActivity.class)));

        // Stats button
        MaterialButton btnStats = findViewById(R.id.btnStats);
        btnStats.setOnClickListener(v -> startActivity(new Intent(this, StatsActivity.class)));

        // Shopping list button
        MaterialButton btnShopping = findViewById(R.id.btnShopping);
        btnShopping.setOnClickListener(v -> startActivity(new Intent(this, ShoppingListActivity.class)));

        // Long-press the app name to fire a test reminder (handy for the demo video).
        findViewById(R.id.tvBrand).setOnLongClickListener(v -> {
            ReminderReceiver.notifyNow(this, true);
            return true;
        });

        ReminderScheduler.schedule(this);
        askNotificationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<PantryItem> items = db.getActive();
        adapter.submit(items);

        int attention = 0;
        for (PantryItem item : items) {
            if (item.daysLeft() <= 3) attention++;
        }

        if (items.isEmpty()) {
            tvHeadline.setText("Nothing in your pantry yet");
        } else if (attention == 0) {
            tvHeadline.setText("Everything is fresh");
        } else if (attention == 1) {
            tvHeadline.setText("1 item needs attention");
        } else {
            tvHeadline.setText(attention + " items need attention");
        }

        int used = db.count(PantryItem.USED);
        int wasted = db.count(PantryItem.WASTED);
        tvTally.setText(used + " used in time, " + wasted + " wasted so far");

        boolean empty = items.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvPantry.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvListNote.setVisibility(empty ? View.GONE : View.VISIBLE);
        btnRecipes.setEnabled(!empty);

        // Update shopping badge
        int shopCount = db.shoppingListCount();
        MaterialButton btnShopping = findViewById(R.id.btnShopping);
        if (shopCount > 0) {
            btnShopping.setText("Shopping list (" + shopCount + ")");
        } else {
            btnShopping.setText("Shopping list");
        }
    }

    private void showItemActions(PantryItem item) {
        String[] actions = {"Mark as used", "Mark as wasted", "Remove from pantry"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(item.name)
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        db.setStatus(item.id, PantryItem.USED);
                        promptAddToShoppingList(item.name);
                    } else if (which == 1) {
                        db.setStatus(item.id, PantryItem.WASTED);
                        promptAddToShoppingList(item.name);
                    } else {
                        db.delete(item.id);
                    }
                    refresh();
                })
                .show();
    }

    /** After marking an item as used/wasted, offer to add it to the shopping list for restocking. */
    private void promptAddToShoppingList(String itemName) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Restock " + itemName + "?")
                .setMessage("Add it to your shopping list so you remember to buy more.")
                .setPositiveButton("Add to list", (d, w) -> {
                    db.addToShoppingList(itemName);
                    Toast.makeText(this, itemName + " added to shopping list", Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNegativeButton("No thanks", null)
                .show();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}