package com.aspire.ecoplate;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ShoppingListActivity extends AppCompatActivity implements ShoppingAdapter.Listener {

    private DatabaseHelper db;
    private ShoppingAdapter adapter;
    private View emptyState;
    private MaterialButton btnClearChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        Ui.edgeToEdge(this, findViewById(R.id.shoppingRoot));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        db = new DatabaseHelper(this);

        emptyState = findViewById(R.id.emptyShoppingState);
        btnClearChecked = findViewById(R.id.btnClearChecked);

        adapter = new ShoppingAdapter(this);
        RecyclerView rv = findViewById(R.id.rvShopping);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnClearChecked.setOnClickListener(v -> {
            db.clearCheckedShoppingItems();
            refresh();
        });

        refresh();
    }

    private void refresh() {
        List<ShoppingItem> list = db.getShoppingList();
        adapter.submit(list);

        boolean empty = list.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        btnClearChecked.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onCheckedChanged(ShoppingItem item, boolean checked) {
        db.toggleShoppingItem(item.id, checked);
        item.checked = checked;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDelete(ShoppingItem item) {
        db.deleteShoppingItem(item.id);
        refresh();
    }
}
