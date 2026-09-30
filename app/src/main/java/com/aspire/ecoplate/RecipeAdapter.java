package com.aspire.ecoplate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.VH> {

    public interface OnCookedClick {
        void onCooked(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private OnCookedClick cookedListener;

    public void setOnCookedClickListener(OnCookedClick listener) {
        this.cookedListener = listener;
    }

    public void submit(List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Recipe r = recipes.get(position);
        h.tvTitle.setText(r.title);

        StringBuilder meta = new StringBuilder();
        if (r.minutes > 0) {
            meta.append(r.minutes).append(" min");
        }
        if (r.uses != null && !r.uses.isEmpty()) {
            if (meta.length() > 0) meta.append(", ");
            meta.append("uses ").append(r.uses);
        }
        h.tvMeta.setText(meta.toString());
        h.tvSteps.setText(r.steps);

        h.btnCooked.setOnClickListener(v -> {
            if (cookedListener != null) cookedListener.onCooked(r);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvMeta;
        final TextView tvSteps;
        final MaterialButton btnCooked;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvSteps = itemView.findViewById(R.id.tvSteps);
            btnCooked = itemView.findViewById(R.id.btnCooked);
        }
    }
}
