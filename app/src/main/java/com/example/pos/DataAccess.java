package com.example.pos;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataAccess {
    private DatabaseHelper dbHelper;

    public DataAccess(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long insertTransaction(double amount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TRANSACTION_AMOUNT, amount);

        long newRowId = db.insert(DatabaseHelper.TABLE_TRANSACTION, null, values);

        db.close();

        return newRowId;
    }

    public long insertUser(String name, String fullName, String password, String category, String status, String createdAt, String updatedAt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if a user with the same name already exists (case-insensitive comparison)
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USER_NAME + " COLLATE NOCASE = ?",
                new String[]{name},
                null,
                null,
                null
        );

        // Check if the cursor has any rows, indicating that a user with the same name already exists
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return -1; // Return -1 to indicate that the insertion failed due to a duplicate user name
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, name);
        values.put(DatabaseHelper.COLUMN_FUll_NAME, fullName);
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_CATEGORY, category);
        values.put(DatabaseHelper.COLUMN_STATUS, status);
        values.put(DatabaseHelper.COLUMN_USER_CREATED_AT, createdAt);
        values.put(DatabaseHelper.COLUMN_USER_UPDATED_AT, updatedAt);

        long newRowId = db.insert(DatabaseHelper.TABLE_USERS, null, values);

        cursor.close();
        db.close();

        return newRowId;
    }

    public boolean loginUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {DatabaseHelper.COLUMN_USER_ID};
        String selection = DatabaseHelper.COLUMN_USER_NAME + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean loginSuccessful = (cursor != null && cursor.getCount() > 0);

        cursor.close();
        db.close();

        return loginSuccessful;
    }

    public void saveLoggedInUser(Context context, String username) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Session", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("username", username);
        editor.apply();
    }

    public boolean getLoggedInUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Session", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    public void logoutUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Session", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Create an Intent to navigate to the login screen
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        Toast.makeText(context, "Logged Out Success", Toast.LENGTH_SHORT).show();

    }


    public Cursor getStaffAccounts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_USER_NAME,
                DatabaseHelper.COLUMN_FUll_NAME,
                DatabaseHelper.COLUMN_CATEGORY,
                DatabaseHelper.COLUMN_STATUS,
                DatabaseHelper.COLUMN_USER_CREATED_AT
        };

        String selection = DatabaseHelper.COLUMN_CATEGORY + " = ?";
        String[] selectionArgs = { "Staff" }; // Modify this according to your category value

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getAdminAccounts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_USER_NAME,
                DatabaseHelper.COLUMN_FUll_NAME,
                DatabaseHelper.COLUMN_CATEGORY,
                DatabaseHelper.COLUMN_STATUS,
                DatabaseHelper.COLUMN_USER_CREATED_AT
        };

        String selection = DatabaseHelper.COLUMN_CATEGORY + " = ?";
        String[] selectionArgs = { "Admin" }; // Modify this according to your category value

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }

    public long insertItem(String name, String category, int quantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if an item with the same name already exists (case-insensitive comparison)
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_INVENTORY,
                null,
                DatabaseHelper.COLUMN_ITEM_NAME + " COLLATE NOCASE = ?",
                new String[]{name},
                null,
                null,
                null
        );

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String date_in = currentDateTime.toString();

        // The item does not exist, proceed with the insertion
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ITEM_NAME, name);
        values.put(DatabaseHelper.COLUMN_ITEM_CATEGORY, category);
        values.put(DatabaseHelper.COLUMN_ITEM_DATE_IN, date_in);
        values.put(DatabaseHelper.COLUMN_ITEM_DATE_OUT, "Not Out");
        values.put(DatabaseHelper.COLUMN_ITEM_ADDED_BY, "Null");
        values.put(DatabaseHelper.COLUMN_ITEM_UPDATED_BY, "Null");
        values.put(DatabaseHelper.COLUMN_ITEM_UPDATED_AT, "Null");

        long newRowId = -1;
        if (quantity > 0) {
            db.beginTransaction();
            try {
                for (int i = 0; i < quantity; i++) {
                    newRowId = db.insert(DatabaseHelper.TABLE_INVENTORY, null, values);
                    if (newRowId == -1) {
                        // Insertion failed, handle the error
                        break;
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        cursor.close();
        db.close();

        return newRowId;
    }

    public void updateInventoryItem(String previousItemName, String updatedItemName, String category, Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ITEM_NAME, updatedItemName);
        values.put(DatabaseHelper.COLUMN_ITEM_CATEGORY, category);

        String selection = DatabaseHelper.COLUMN_ITEM_NAME + " = ?";
        String[] selectionArgs = {previousItemName};

        int rowsAffected = db.update(DatabaseHelper.TABLE_INVENTORY, values, selection, selectionArgs);

        if (rowsAffected > 0) {
            Toast.makeText(context, "Item updated: " + updatedItemName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteInventoryItems(String itemName, int quantity, Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_ITEM_NAME + " = ?";
        String[] whereArgs = {itemName};

        int rowsDeleted;
        if (quantity <= 0) {
            // Delete all rows with the provided item name
            rowsDeleted = db.delete(DatabaseHelper.TABLE_INVENTORY, whereClause, whereArgs);
        } else {
            // Delete a specific number of rows with the provided item name
            String query = "SELECT " + DatabaseHelper.COLUMN_ITEM_ID +
                    " FROM " + DatabaseHelper.TABLE_INVENTORY +
                    " WHERE " + whereClause +
                    " LIMIT " + quantity;

            Cursor cursor = db.rawQuery(query, whereArgs);
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int itemId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_ID));
                    db.delete(DatabaseHelper.TABLE_INVENTORY, DatabaseHelper.COLUMN_ITEM_ID + " = ?", new String[]{String.valueOf(itemId)});
                } while (cursor.moveToNext() && --quantity > 0);
            }
            cursor.close();

            rowsDeleted = quantity;
        }

        if (rowsDeleted > 0) {
            Toast.makeText(context, "Deleted " + quantity + " " + itemName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "No rows deleted", Toast.LENGTH_SHORT).show();
        }
    }



    public List<String> getUniqueItemNames() {
        List<String> uniqueItemNames = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT " + DatabaseHelper.COLUMN_ITEM_NAME + " FROM " + DatabaseHelper.TABLE_INVENTORY;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_NAME));
                uniqueItemNames.add(itemName);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return uniqueItemNames;
    }



    public Cursor getAllInventoryItems() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_ITEM_ID,
                DatabaseHelper.COLUMN_ITEM_NAME,
                DatabaseHelper.COLUMN_ITEM_DATE_IN
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_INVENTORY,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getDisposableItems() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_ITEM_ID,
                DatabaseHelper.COLUMN_ITEM_NAME,
                DatabaseHelper.COLUMN_ITEM_DATE_IN
        };

        String selection = DatabaseHelper.COLUMN_ITEM_CATEGORY + " = ?";
        String[] selectionArgs = { "Disposables" }; // Modify this according to your category value

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_INVENTORY,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getBeveragesItem() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_ITEM_ID,
                DatabaseHelper.COLUMN_ITEM_NAME,
                DatabaseHelper.COLUMN_ITEM_DATE_IN
        };

        String selection = DatabaseHelper.COLUMN_ITEM_CATEGORY + " = ?";
        String[] selectionArgs = { "Beverages" }; // Modify this according to your category value

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_INVENTORY,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getFoodItem() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_ITEM_ID,
                DatabaseHelper.COLUMN_ITEM_NAME,
                DatabaseHelper.COLUMN_ITEM_DATE_IN
        };

        String selection = DatabaseHelper.COLUMN_ITEM_CATEGORY + " = ?";
        String[] selectionArgs = { "Food" }; // Modify this according to your category value

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_INVENTORY,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getIngredientsItem() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_ITEM_ID,
                DatabaseHelper.COLUMN_ITEM_NAME,
                DatabaseHelper.COLUMN_ITEM_DATE_IN
        };

        String selection = DatabaseHelper.COLUMN_ITEM_CATEGORY + " = ?";
        String[] selectionArgs = { "Ingredients" }; // Modify this according to your category value

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_INVENTORY,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getPastriesItem() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_ITEM_ID,
                DatabaseHelper.COLUMN_ITEM_NAME,
                DatabaseHelper.COLUMN_ITEM_DATE_IN
        };

        String selection = DatabaseHelper.COLUMN_ITEM_CATEGORY + " = ?";
        String[] selectionArgs = { "Pastries" }; // Modify this according to your category value

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_INVENTORY,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }


    public Cursor getItemById(int itemId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_ITEM_ID,
                DatabaseHelper.COLUMN_ITEM_NAME,
                DatabaseHelper.COLUMN_ITEM_DATE_IN
        };

        String selection = DatabaseHelper.COLUMN_ITEM_ID + " = ?";
        String[] selectionArgs = { String.valueOf(itemId) };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_INVENTORY,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }

// Add more methods as needed for inserting, updating, deleting, and retrieving specific data from the database.



}
