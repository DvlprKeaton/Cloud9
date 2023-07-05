package com.example.pos;

import static com.example.pos.DatabaseHelper.COLUMN_LAST_ORDER_NUMBER;
import static com.example.pos.DatabaseHelper.TABLE_ORDER_NUMBER;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataAccess {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DataAccess(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
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

    public Map<String, String> loginUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_CATEGORY};
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

        Map<String, String> loginDetails = new HashMap<>();

        boolean loginSuccessful = (cursor != null && cursor.getCount() > 0);
        loginDetails.put("loginSuccessful", String.valueOf(loginSuccessful));

        if (loginSuccessful && cursor.moveToFirst()) {
            int roleColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY);
            String fetchedRole = cursor.getString(roleColumnIndex);
            loginDetails.put("userRole", fetchedRole);
        }

        cursor.close();
        db.close();

        return loginDetails;
    }


    public void saveLoggedInUser(Context context, String username, String userRole) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Session", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("username", username);
        editor.putString("userRole", userRole);
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

    public long insertMenu(String name, String category, String price) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if an item with the same name already exists (case-insensitive comparison)
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MENU,
                null,
                DatabaseHelper.COLUMN_MENU_NAME + " COLLATE NOCASE = ?",
                new String[]{name},
                null,
                null,
                null
        );

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();

        // The item does not exist, proceed with the insertion
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_MENU_NAME, name);
        values.put(DatabaseHelper.COLUMN_MENU_CATEGORY, category);
        values.put(DatabaseHelper.COLUMN_MENU_PRICE, price);
        values.put(DatabaseHelper.COLUMN_MENU_ADDED_BY, "Null");
        values.put(DatabaseHelper.COLUMN_MENU_UPDATED_BY, "Null");
        values.put(DatabaseHelper.COLUMN_MENU_UPDATED_AT, updatedAt);

        long newRowId = db.insert(DatabaseHelper.TABLE_MENU, null, values);

        cursor.close();
        db.close();

        return newRowId;
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

    public List<ButtonData> getProductData(String filter) {
        List<ButtonData> buttonDataList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Build the WHERE clause based on the filter
        String whereClause = "";
        String[] whereArgs = null;
        if (filter.equals("Espresso")) {
            // Add conditions specific to FragmentA
            whereClause = "category = ? OR category = ?";
            whereArgs = new String[]{"H Espresso", "C Espresso"};
        } else if (filter.equals("Frappe")) {
            // Add conditions specific to FragmentB
            whereClause = "category = ?";
            whereArgs = new String[]{"Frappe"};
        } else if (filter.equals("Fruit Tea")) {
            // Add conditions specific to FragmentB
            whereClause = "category = ?";
            whereArgs = new String[]{"Fruit Tea"};
        } else if (filter.equals("Non Espresso")) {
            // Add conditions specific to FragmentB
            whereClause = "category = ?";
            whereArgs = new String[]{"Non Espresso"};
        } else if (filter.equals("Sparkling Ade")) {
            // Add conditions specific to FragmentB
            whereClause = "category = ?";
            whereArgs = new String[]{"Sparkling Ade"};
        } else if (filter.equals("Add Ons")) {
            // Add conditions specific to FragmentB
            whereClause = "category = ?";
            whereArgs = new String[]{"Add Ons"};
        }

        // Execute the query to fetch button names and prices from the "menu" table with the WHERE clause
        Cursor cursor = db.query("menu", new String[]{"name", "price"}, whereClause, whereArgs, null, null, null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String buttonName = cursor.getString(cursor.getColumnIndex("name"));
            @SuppressLint("Range") double buttonPrice = cursor.getDouble(cursor.getColumnIndex("price"));
            ButtonData buttonData = new ButtonData(buttonName, buttonPrice);
            buttonDataList.add(buttonData);
        }

        // Close the cursor and database connection
        cursor.close();
        db.close();

        return buttonDataList;
    }

    public long insertPendingOrders(String name, int quantity, String type, double discount, String discountType, String paymentType, double payment, double total, double change, String addedBy) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();

        // Insert the pending order
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER, generateOrderNumber(db, false)); // You need to implement the generateOrderNumber() method with the database parameter
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_NAME, name);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_QUANTITY, quantity);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_TYPE, type);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_DISCOUNT, discount);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_DISCOUNT_TYPE, discountType);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_PAYMENT_TYPE, paymentType);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_PAYMENT, payment);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_TOTAL, total);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_CHANGE, change);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_BY, addedBy);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_AT, updatedAt);

        long newRowId = db.insert(DatabaseHelper.TABLE_PENDING_ORDERS, null, values);

        return newRowId;
    }

    @SuppressLint("Range")
    private int generateOrderNumber(SQLiteDatabase db, boolean isCheckoutButtonClicked) {
        // Retrieve the last order number from the table
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_LAST_ORDER_NUMBER + " FROM " + TABLE_ORDER_NUMBER + " ORDER BY " + COLUMN_LAST_ORDER_NUMBER + " DESC LIMIT 1", null);

        int orderNumber = 0;

        if (cursor.moveToFirst()) {
            orderNumber = cursor.getInt(cursor.getColumnIndex(COLUMN_LAST_ORDER_NUMBER));
        }

        cursor.close();

        // Increment the order number for the next order only if the checkout button is clicked
        if (isCheckoutButtonClicked) {
            int nextOrderNumber = orderNumber + 1;

            // Update the order number in the table
            ContentValues updateValues = new ContentValues();
            updateValues.put(COLUMN_LAST_ORDER_NUMBER, nextOrderNumber);
            db.update(TABLE_ORDER_NUMBER, updateValues, null, null);
        }

        return orderNumber;
    }



}
