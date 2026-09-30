package com.aspire.ecoplate;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault());

    private TextInputEditText etName;
    private TextInputEditText etQty;
    private TextInputEditText etDate;
    private ChipGroup chipGroup;

    private Long expiryDay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Ui.edgeToEdge(this, findViewById(R.id.addRoot));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        etQty = findViewById(R.id.etQty);
        etDate = findViewById(R.id.etDate);
        chipGroup = findViewById(R.id.chipGroup);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            int days;
            if (id == R.id.chip3) {
                days = 3;
            } else if (id == R.id.chip7) {
                days = 7;
            } else if (id == R.id.chip14) {
                days = 14;
            } else {
                days = 30;
            }
            setExpiry(LocalDate.now().plusDays(days).toEpochDay());
        });

        etDate.setOnClickListener(v -> openDatePicker());
        btnSave.setOnClickListener(v -> save());
    }

    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Expiry date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            LocalDate date = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate();
            chipGroup.clearCheck();
            setExpiry(date.toEpochDay());
        });
        picker.show(getSupportFragmentManager(), "expiry_date");
    }

    private void setExpiry(long epochDay) {
        expiryDay = epochDay;
        etDate.setText(LocalDate.ofEpochDay(epochDay).format(DATE_FORMAT));
    }

    private void save() {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String qty = etQty.getText() == null ? "" : etQty.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Enter the item name");
            etName.requestFocus();
            return;
        }
        if (expiryDay == null) {
            Toast.makeText(this, "Choose when it expires", Toast.LENGTH_SHORT).show();
            return;
        }

        new DatabaseHelper(this).insert(name, qty, expiryDay);
        Toast.makeText(this, name + " added", Toast.LENGTH_SHORT).show();
        finish();
    }
}
