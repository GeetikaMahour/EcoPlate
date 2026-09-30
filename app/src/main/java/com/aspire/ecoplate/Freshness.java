package com.aspire.ecoplate;

import android.content.Context;

import androidx.core.content.ContextCompat;

/** Turns days-left into the label, colour and bar length the UI shows. */
final class Freshness {

    private Freshness() {}

    static int color(Context ctx, long daysLeft) {
        int res;
        if (daysLeft <= 0) {
            res = R.color.eco_urgent;
        } else if (daysLeft <= 3) {
            res = R.color.eco_soon;
        } else {
            res = R.color.eco_fresh;
        }
        return ContextCompat.getColor(ctx, res);
    }

    static String label(long daysLeft) {
        if (daysLeft < 0) return "Expired";
        if (daysLeft == 0) return "Today";
        if (daysLeft == 1) return "Tomorrow";
        return daysLeft + " days";
    }

    /** Share of the item's shelf life that is still left, 0 to 100. */
    static int percent(PantryItem item) {
        long total = Math.max(1, item.expiryDay - item.addedDay);
        long left = item.daysLeft();
        if (left < 0) return 0;
        int pct = (int) Math.min(100, (left * 100) / total);
        return Math.max(pct, 6);
    }
}
