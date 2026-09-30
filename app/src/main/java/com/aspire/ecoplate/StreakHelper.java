package com.aspire.ecoplate;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;

/**
 * Tracks a "items saved this week" streak counter using SharedPreferences.
 * Resets weekly (Monday-based) to keep motivation fresh.
 */
public final class StreakHelper {

    private static final String PREFS = "ecoplate_streak";
    private static final String KEY_COUNT = "week_count";
    private static final String KEY_WEEK = "week_epoch";

    private StreakHelper() {}

    /** Returns the current week number (epoch day / 7). */
    private static long currentWeek() {
        return LocalDate.now().toEpochDay() / 7;
    }

    /** Record that the user cooked items — increment counter by the number of items saved. */
    public static int recordCooked(Context context, int itemsSaved) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long storedWeek = prefs.getLong(KEY_WEEK, -1);
        long thisWeek = currentWeek();

        int current;
        if (storedWeek == thisWeek) {
            current = prefs.getInt(KEY_COUNT, 0) + itemsSaved;
        } else {
            current = itemsSaved;
        }

        prefs.edit()
                .putInt(KEY_COUNT, current)
                .putLong(KEY_WEEK, thisWeek)
                .apply();

        return current;
    }

    /** Get how many items were saved this week. Resets if the week has changed. */
    public static int getWeekCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long storedWeek = prefs.getLong(KEY_WEEK, -1);
        if (storedWeek != currentWeek()) return 0;
        return prefs.getInt(KEY_COUNT, 0);
    }
}
