package com.aspire.ecoplate;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity {

    private static final DateTimeFormatter WEEK_FMT =
            DateTimeFormatter.ofPattern("d MMM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Ui.edgeToEdge(this, findViewById(R.id.statsRoot));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        DatabaseHelper db = new DatabaseHelper(this);

        int usedTotal = db.count(PantryItem.USED);
        int wastedTotal = db.count(PantryItem.WASTED);
        int total = usedTotal + wastedTotal;

        ((TextView) findViewById(R.id.tvUsedCount)).setText(String.valueOf(usedTotal));
        ((TextView) findViewById(R.id.tvWastedCount)).setText(String.valueOf(wastedTotal));

        // Save rate
        int saveRate = total > 0 ? (usedTotal * 100) / total : 0;
        TextView tvSaveRate = findViewById(R.id.tvSaveRate);
        LinearProgressIndicator progressSaveRate = findViewById(R.id.progressSaveRate);
        tvSaveRate.setText(saveRate + "%");
        progressSaveRate.setProgress(saveRate);

        if (total == 0) {
            tvSaveRate.setText("—");
            progressSaveRate.setProgress(0);
        }

        // Weekly streak
        int streak = StreakHelper.getWeekCount(this);
        TextView tvStreakTitle = findViewById(R.id.tvStreakTitle);
        TextView tvStreakSub = findViewById(R.id.tvStreakSub);
        if (streak == 0) {
            tvStreakTitle.setText("No items saved this week yet");
            tvStreakSub.setText("Cook a recipe and mark ingredients as used to start your streak!");
        } else if (streak == 1) {
            tvStreakTitle.setText("🌱 1 item saved this week!");
            tvStreakSub.setText("Great start — keep going to build your streak!");
        } else {
            tvStreakTitle.setText(" " + streak + " items saved this week!");
            tvStreakSub.setText("You're on fire! Every item saved means less food wasted.");
        }

        // Weekly trend bars
        LinearLayout trendContainer = findViewById(R.id.trendContainer);
        List<int[]> weeklyStats = db.getWeeklyStats(6);
        int maxVal = 1;
        for (int[] w : weeklyStats) {
            maxVal = Math.max(maxVal, Math.max(w[1], w[2]));
        }

        for (int[] week : weeklyStats) {
            long weekStart = week[0];
            int used = week[1];
            int wasted = week[2];

            View row = createTrendRow(weekStart, used, wasted, maxVal);
            trendContainer.addView(row);
        }
    }

    private View createTrendRow(long weekStartEpochDay, int used, int wasted, int max) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (8 * getResources().getDisplayMetrics().density);
        row.setPadding(0, pad, 0, pad);

        // Week label
        String weekLabel = LocalDate.ofEpochDay(weekStartEpochDay).format(WEEK_FMT)
                + " – " + LocalDate.ofEpochDay(weekStartEpochDay + 6).format(WEEK_FMT);
        TextView tvWeek = new TextView(this);
        tvWeek.setText(weekLabel);
        tvWeek.setTextColor(ContextCompat.getColor(this, R.color.eco_muted));
        tvWeek.setTextSize(13);
        row.addView(tvWeek);

        // Used bar
        LinearLayout usedRow = createBarRow("Used", used, max, R.color.eco_fresh);
        row.addView(usedRow);

        // Wasted bar
        LinearLayout wastedRow = createBarRow("Wasted", wasted, max, R.color.eco_urgent);
        row.addView(wastedRow);

        return row;
    }

    private LinearLayout createBarRow(String label, int value, int max, int colorRes) {
        LinearLayout barRow = new LinearLayout(this);
        barRow.setOrientation(LinearLayout.HORIZONTAL);
        barRow.setGravity(Gravity.CENTER_VERTICAL);
        int topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = topMargin;
        barRow.setLayoutParams(rowParams);

        // Label
        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(12);
        tvLabel.setTextColor(ContextCompat.getColor(this, R.color.eco_muted));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                (int) (60 * getResources().getDisplayMetrics().density),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLabel.setLayoutParams(labelParams);
        barRow.addView(tvLabel);

        // Bar
        View bar = new View(this);
        int barWidth = max > 0 ? Math.max((int) (((float) value / max) *
                (getResources().getDisplayMetrics().widthPixels - 200)), 4) : 4;
        int barHeight = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(barWidth, barHeight);
        barParams.setMarginStart((int) (4 * getResources().getDisplayMetrics().density));
        bar.setLayoutParams(barParams);
        bar.setBackgroundColor(ContextCompat.getColor(this, colorRes));
        // Rounded corners by using a shape with clip
        bar.setBackground(createRoundedDrawable(ContextCompat.getColor(this, colorRes)));
        barRow.addView(bar);

        // Count text
        TextView tvCount = new TextView(this);
        tvCount.setText(String.valueOf(value));
        tvCount.setTextSize(13);
        tvCount.setTextColor(ContextCompat.getColor(this, R.color.eco_ink));
        tvCount.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        countParams.setMarginStart((int) (6 * getResources().getDisplayMetrics().density));
        tvCount.setLayoutParams(countParams);
        barRow.addView(tvCount);

        return barRow;
    }

    private android.graphics.drawable.GradientDrawable createRoundedDrawable(int color) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadius(8 * getResources().getDisplayMetrics().density);
        return drawable;
    }
}
