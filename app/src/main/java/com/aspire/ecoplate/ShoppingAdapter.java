package com.aspire.ecoplate;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.VH> {

    public interface Listener {
        void onCheckedChanged(ShoppingItem item, boolean checked);
        void onDelete(ShoppingItem item);
    }

    private final List<ShoppingItem> items = new ArrayList<>();
    private final Listener listener;

    public ShoppingAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<ShoppingItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ShoppingItem item = items.get(position);
        h.tvName.setText(item.name);
        h.cbItem.setOnCheckedChangeListener(null);  // avoid recursive call
        h.cbItem.setChecked(item.checked);

        // Strike-through text for checked items
        if (item.checked) {
            h.tvName.setPaintFlags(h.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvName.setAlpha(0.5f);
        } else {
            h.tvName.setPaintFlags(h.tvName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvName.setAlpha(1.0f);
        }

        h.cbItem.setOnCheckedChangeListener((btn, checked) ->
                listener.onCheckedChanged(item, checked));

        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final MaterialCheckBox cbItem;
        final TextView tvName;
        final ImageView btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            cbItem = itemView.findViewById(R.id.cbItem);
            tvName = itemView.findViewById(R.id.tvShopName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
