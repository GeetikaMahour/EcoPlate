package com.aspire.ecoplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PantryItem {

    public static final int ACTIVE = 0;
    public static final int USED = 1;
    public static final int WASTED = 2;

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault());

    public long id;
    public String name;
    public String quantity;
    public long expiryDay;   // LocalDate epoch day
    public long addedDay;    // LocalDate epoch day
    public int status;

    /** Days until expiry. Negative means already expired, 0 means expires today. */
    public long daysLeft() {
        return expiryDay - LocalDate.now().toEpochDay();
    }

    public String expiryText() {
        return LocalDate.ofEpochDay(expiryDay).format(FORMAT);
    }
}
