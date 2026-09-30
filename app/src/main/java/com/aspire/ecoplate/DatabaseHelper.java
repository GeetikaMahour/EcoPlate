package com.aspire.ecoplate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ecoplate.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE = "items";
    private static final String SHOP_TABLE = "shopping_list";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity TEXT, "
                + "expiry_day INTEGER NOT NULL, "
                + "added_day INTEGER NOT NULL, "
                + "status INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE " + SHOP_TABLE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "checked INTEGER NOT NULL DEFAULT 0, "
                + "added_day INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + SHOP_TABLE + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "checked INTEGER NOT NULL DEFAULT 0, "
                    + "added_day INTEGER NOT NULL)");
        }
    }

    public long insert(String name, String quantity, long expiryDay) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("expiry_day", expiryDay);
        values.put("added_day", LocalDate.now().toEpochDay());
        values.put("status", PantryItem.ACTIVE);
        return getWritableDatabase().insert(TABLE, null, values);
    }

    /** All items still in the pantry, soonest to expire first. */
    public List<PantryItem> getActive() {
        List<PantryItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(
                TABLE, null, "status = ?", new String[]{String.valueOf(PantryItem.ACTIVE)},
                null, null, "expiry_day ASC, name ASC");
        try {
            while (c.moveToNext()) {
                PantryItem item = new PantryItem();
                item.id = c.getLong(c.getColumnIndexOrThrow("id"));
                item.name = c.getString(c.getColumnIndexOrThrow("name"));
                String qty = c.getString(c.getColumnIndexOrThrow("quantity"));
                item.quantity = qty == null ? "" : qty;
                item.expiryDay = c.getLong(c.getColumnIndexOrThrow("expiry_day"));
                item.addedDay = c.getLong(c.getColumnIndexOrThrow("added_day"));
                item.status = c.getInt(c.getColumnIndexOrThrow("status"));
                list.add(item);
            }
        } finally {
            c.close();
        }
        return list;
    }

    public void setStatus(long id, int status) {
        ContentValues values = new ContentValues();
        values.put("status", status);
        getWritableDatabase().update(TABLE, values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void delete(long id) {
        getWritableDatabase().delete(TABLE, "id = ?", new String[]{String.valueOf(id)});
    }

    public int count(int status) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE + " WHERE status = ?",
                new String[]{String.valueOf(status)});
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    // ---- Stats helpers (Feature B) ----

    /** Returns weekly counts for used / wasted items over the last N weeks.
     *  Each int[] has { epochDayOfWeekStart, usedCount, wastedCount }. */
    public List<int[]> getWeeklyStats(int weeks) {
        List<int[]> stats = new ArrayList<>();
        long today = LocalDate.now().toEpochDay();
        for (int w = weeks - 1; w >= 0; w--) {
            long weekStart = today - (w * 7L) - 6;
            long weekEnd = today - (w * 7L);
            int used = countInRange(PantryItem.USED, weekStart, weekEnd);
            int wasted = countInRange(PantryItem.WASTED, weekStart, weekEnd);
            stats.add(new int[]{(int) weekStart, used, wasted});
        }
        return stats;
    }

    private int countInRange(int status, long dayStart, long dayEnd) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE
                        + " WHERE status = ? AND added_day >= ? AND added_day <= ?",
                new String[]{String.valueOf(status), String.valueOf(dayStart), String.valueOf(dayEnd)});
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    // ---- Shopping list helpers (Feature C) ----

    public long addToShoppingList(String name) {
        // Don't add duplicates
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM " + SHOP_TABLE + " WHERE name = ? AND checked = 0",
                new String[]{name});
        try {
            if (c.moveToFirst()) return c.getLong(0);
        } finally {
            c.close();
        }
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("checked", 0);
        values.put("added_day", LocalDate.now().toEpochDay());
        return getWritableDatabase().insert(SHOP_TABLE, null, values);
    }

    public List<ShoppingItem> getShoppingList() {
        List<ShoppingItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(
                SHOP_TABLE, null, null, null,
                null, null, "checked ASC, added_day DESC");
        try {
            while (c.moveToNext()) {
                ShoppingItem item = new ShoppingItem();
                item.id = c.getLong(c.getColumnIndexOrThrow("id"));
                item.name = c.getString(c.getColumnIndexOrThrow("name"));
                item.checked = c.getInt(c.getColumnIndexOrThrow("checked")) == 1;
                list.add(item);
            }
        } finally {
            c.close();
        }
        return list;
    }

    public void toggleShoppingItem(long id, boolean checked) {
        ContentValues values = new ContentValues();
        values.put("checked", checked ? 1 : 0);
        getWritableDatabase().update(SHOP_TABLE, values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteShoppingItem(long id) {
        getWritableDatabase().delete(SHOP_TABLE, "id = ?", new String[]{String.valueOf(id)});
    }

    public void clearCheckedShoppingItems() {
        getWritableDatabase().delete(SHOP_TABLE, "checked = 1", null);
    }

    public int shoppingListCount() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + SHOP_TABLE + " WHERE checked = 0", null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    /** Find an active pantry item by name (case-insensitive, whitespace-trimmed). */
    public PantryItem findActiveByName(String name) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE + " WHERE status = ? AND LOWER(name) = LOWER(?) LIMIT 1",
                new String[]{String.valueOf(PantryItem.ACTIVE), name.trim()});
        try {
            if (c.moveToFirst()) {
                PantryItem item = new PantryItem();
                item.id = c.getLong(c.getColumnIndexOrThrow("id"));
                item.name = c.getString(c.getColumnIndexOrThrow("name"));
                String qty = c.getString(c.getColumnIndexOrThrow("quantity"));
                item.quantity = qty == null ? "" : qty;
                item.expiryDay = c.getLong(c.getColumnIndexOrThrow("expiry_day"));
                item.addedDay = c.getLong(c.getColumnIndexOrThrow("added_day"));
                item.status = c.getInt(c.getColumnIndexOrThrow("status"));
                return item;
            }
        } finally {
            c.close();
        }
        return null;
    }
}
