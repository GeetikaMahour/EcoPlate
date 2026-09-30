package com.aspire.ecoplate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {

    public interface OnItemClick {
        void onClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnItemClick listener;

    public PantryAdapter(OnItemClick listener) {
        this.listener = listener;
    }

    public void submit(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PantryItem item = items.get(position);
        long days = item.daysLeft();
        int color = Freshness.color(h.itemView.getContext(), days);

        h.tvName.setText(item.name);
        String meta = "Expires " + item.expiryText();
        if (!item.quantity.isEmpty()) {
            meta += ", " + item.quantity;
        }
        h.tvMeta.setText(meta);
        h.tvDays.setText(Freshness.label(days));
        h.tvDays.setTextColor(color);
        h.bar.setIndicatorColor(color);
        h.bar.setProgress(Freshness.percent(item));
        h.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvMeta;
        final TextView tvDays;
        final LinearProgressIndicator bar;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvDays = itemView.findViewById(R.id.tvDays);
            bar = itemView.findViewById(R.id.bar);
        }
    }
}
