package com.example.pos;

import static com.example.pos.DatabaseHelper.COLUMN_GCASH_REFERENCE_NUMBER;
import static com.example.pos.DatabaseHelper.COLUMN_LAST_ORDER_NUMBER;
import static com.example.pos.DatabaseHelper.COLUMN_LAST_RECEIPT_NUMBER;
import static com.example.pos.DatabaseHelper.COLUMN_ORDER_NUMBER;
import static com.example.pos.DatabaseHelper.TABLE_ORDER_NUMBER;
import static com.example.pos.DatabaseHelper.TABLE_RECEIPT_NUMBER;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
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

    public long insertAdmin(String name, String fullName, String password, String category, String status, String createdAt, String updatedAt) {
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

        loginUser(name,password);

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

        if (cursor.getCount() == 0 && username.equals("Admin")) {
            // Get the current date and time
            LocalDateTime currentDateTime = LocalDateTime.now();
            String date_in = currentDateTime.toString();
            insertAdmin("Admin", "Administrator", password, "Admin", "active", date_in, date_in);
        }

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

    public void updateStaff(String userName, String fullName, String password, String conpassword, String category, int userID, Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        if (userName != null && !userName.isEmpty()) {
            values.put(DatabaseHelper.COLUMN_USER_NAME, userName);
        }
        if (fullName != null && !fullName.isEmpty()) {
            values.put(DatabaseHelper.COLUMN_FUll_NAME, fullName);
        }
        if (password != null && !password.isEmpty()) {
            values.put(DatabaseHelper.COLUMN_PASSWORD, password);
        }
        if (category != null && !category.isEmpty()) {
            values.put(DatabaseHelper.COLUMN_CATEGORY, category);
        }

        if (values.size() > 0) {
            String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userID)};

            // Retrieve the existing values for the selected user
            Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, selection, selectionArgs, null, null, null);
            if (cursor.moveToFirst()) {
                ContentValues updateValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, updateValues);

                // Update only the non-null and non-empty values
                for (String key : values.keySet()) {
                    if (values.getAsString(key) != null && !values.getAsString(key).isEmpty()) {
                        updateValues.put(key, String.valueOf(values.get(key)));
                    }
                }

                int rowsAffected = db.update(DatabaseHelper.TABLE_USERS, updateValues, selection, selectionArgs);

                if (rowsAffected > 0) {
                    Toast.makeText(context, "Update Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            }
            cursor.close();
        } else {
            Toast.makeText(context, "No fields to update", Toast.LENGTH_SHORT).show();
        }
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
            whereClause = "category = ? OR category = ?";
            whereArgs = new String[]{"H Non Espresso", "C Non Espresso"};
        } else if (filter.equals("Sparkling Ade")) {
            // Add conditions specific to FragmentB
            whereClause = "category = ?";
            whereArgs = new String[]{"Sparkling Ade"};
        } else if (filter.equals("Add Ons")) {
            // Add conditions specific to FragmentB
            whereClause = "category = ?";
            whereArgs = new String[]{"Add Ons"};
        }else if (filter.equals("Short Orders")) {
            // Add conditions specific to FragmentB
            whereClause = "category = ?";
            whereArgs = new String[]{"Short Orders"};
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

        // Insert the pending orders
        long newRowId = -1;
        if (quantity > 0) {
            db.beginTransaction();
            try {
                for (int i = 0; i < quantity; i++) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER, generateOrderNumber(db, false)); // You need to implement the generateOrderNumber() method with the database parameter
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_NAME, name);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_QUANTITY, 1); // Set the quantity to 1 for each row
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_TYPE, type);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_DISCOUNT, discount);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_DISCOUNT_TYPE, discountType);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_PAYMENT_TYPE, paymentType);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_PAYMENT, payment);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_TOTAL, total);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_CHANGE, change);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_BY, addedBy);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_AT, updatedAt);

                    newRowId = db.insert(DatabaseHelper.TABLE_PENDING_ORDERS, null, values);
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

        db.close();

        return newRowId;
    }


    public boolean updatePendingOrder(int orderId,String type, String paymentType, String addedBy) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();

        // Prepare the updated values
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_TYPE, type);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_PAYMENT_TYPE, paymentType);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_BY, addedBy);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_AT, updatedAt);

        // Define the WHERE clause to update the specific pending order
        String selection = DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER + " = ?";
        String[] selectionArgs = {String.valueOf(orderId)};

        // Perform the update operation
        int rowsAffected = db.update(DatabaseHelper.TABLE_PENDING_ORDERS, values, selection, selectionArgs);
        if (rowsAffected > 0) {
            System.out.println("Update successful. Rows affected: " + rowsAffected + " " + type + " " + paymentType);
            return true;
        } else {
            System.out.println("Update failed. No rows affected.");
            return false;
        }
    }

    public boolean cashCheckOut(int orderId, double payment, double change, String addedBy, Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();

        // Prepare the updated values
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_PAYMENT, payment);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_CHANGE, change);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_BY, addedBy);
        values.put(DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_AT, updatedAt);

        Log.d("TOTAL", payment + " " + change);

        // Define the WHERE clause to update the specific pending order
        String selection = DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER + " = ?";
        String[] selectionArgs = {String.valueOf(orderId)};

        // Perform the update operation
        int rowsAffected = db.update(DatabaseHelper.TABLE_PENDING_ORDERS, values, selection, selectionArgs);

        pendingToConfirm(orderId, addedBy ,context);

        return rowsAffected > 0;
    }

    public void pendingToConfirm(int orderID, String addedBy, Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define the WHERE clause
        String selection = DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER + " = ?";
        String[] selectionArgs = { String.valueOf(orderID) };

        // Retrieve data from Table TABLE_PENDING_ORDERS with the WHERE clause
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_PENDING_ORDERS + " WHERE " + selection, selectionArgs);

        // Get the column names of Table TABLE_PENDING_ORDERS
        String[] columnNames = cursor.getColumnNames();


        // Prepare the ContentValues object for inserting into Table TABLE_INVENTORY
        ContentValues values = new ContentValues();

        // Iterate through the cursor to transfer data row by row
        while (cursor.moveToNext()) {
            values.clear();

            // Map the values from Table TABLE_PENDING_ORDERS to Table TABLE_ORDERS
            for (String columnName : columnNames) {
                int columnIndex = cursor.getColumnIndex(columnName);
                String columnValue = cursor.getString(columnIndex);
                values.put(columnName, columnValue);
                Log.d("Column Mapping", "Column Name: " + columnName + ", Column Value: " + columnValue);
            }

            // Insert the values into Table TABLE_ORDERS
            long insertedRowId = db.insert(DatabaseHelper.TABLE_ORDERS, null, values);
            Log.d("Insert Result in Table Orders", "Inserted Row ID: " + insertedRowId);
        }

        // Close the cursor after data transfer is complete
        cursor.close();

        // Delete the transferred rows from Table TABLE_PENDING_ORDERS
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_PENDING_ORDERS, selection, selectionArgs);



        if (rowsDeleted > 0) {
            insertReceipt(orderID,addedBy, context);
            generateOrderNumber(db, true);
            Toast.makeText(context, "Order Success", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context, "Order Failed", Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("Range")
    public long insertReceipt(int orderID, String addedBy ,Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();

        // Count the number of rows with the same orderID in the "order" table
        String countQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE " + COLUMN_ORDER_NUMBER + " = " + orderID;

        Cursor cursor = db.rawQuery(countQuery, null);
        int quantity = 0;

        if (cursor.moveToFirst()) {
            quantity = cursor.getInt(0);
        }

        cursor.close();

        String orderType = "";
        String orderDiscount = "";
        String orderDiscountType = "";
        String orderPaymentType = "";
        double orderTotal = 0;
        double orderPayment = 0;
        double orderChange = 0;

        String orderTableQuery = "SELECT * FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE " + COLUMN_ORDER_NUMBER + " = " + orderID;

        Cursor orderCursor = db.rawQuery(orderTableQuery, null);

        String totalQuery = "SELECT SUM(" + DatabaseHelper.COLUMN_ORDER_TOTAL + ") FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE " + COLUMN_ORDER_NUMBER + " = " + orderID;

        Cursor totalCursor = db.rawQuery(totalQuery, null);

        if (totalCursor.moveToFirst()) {
            orderTotal = totalCursor.getDouble(0);
        }

        totalCursor.close();

        Log.d("Sum Payment: ", String.valueOf(orderTotal));

        if (orderCursor.moveToFirst()) {
            orderType = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_TYPE));
            orderDiscount = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_DISCOUNT));
            orderDiscountType = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_DISCOUNT_TYPE));
            orderPaymentType = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_PAYMENT_TYPE));
            orderPayment = orderCursor.getDouble(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_PAYMENT));
            orderChange = orderCursor.getDouble(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_CHANGE));
        }

        orderCursor.close();

        // Insert the receipt
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_RECEIPT_ORDER_NUMBER, orderID);
        values.put(DatabaseHelper.COLUMN_RECEIPT_NUMBER, generateReceiptNumber(db, false));
        values.put(DatabaseHelper.COLUMN_RECEIPT_ORDER_QUANTITY, quantity);
        values.put(DatabaseHelper.COLUMN_RECEIPT_ORDER_TYPE, orderType);
        values.put(DatabaseHelper.COLUMN_RECEIPT_ORDER_DISCOUNT, orderDiscount);
        values.put(DatabaseHelper.COLUMN_RECEIPT_ORDER_DISCOUNT_TYPE, orderDiscountType);
        values.put(DatabaseHelper.COLUMN_RECEIPT_ORDER_PAYMENT_TYPE, orderPaymentType);
        values.put(DatabaseHelper.COLUMN_RECEIPT_ORDER_PAYMENT, orderPayment);
        values.put(DatabaseHelper.COLUMN_RECEIPT_ORDER_TOTAL, orderTotal);
        values.put(DatabaseHelper.COLUMN_RECEIPT_ORDER_CHANGE, orderChange);
        values.put(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_BY, addedBy);
        values.put(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT, updatedAt);

        long newRowId = db.insert(DatabaseHelper.TABLE_RECEIPT, null, values);

        return newRowId;
    }

    public Cursor getReceipt(int orderNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_RECEIPT_ID,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_NUMBER,
                DatabaseHelper.COLUMN_RECEIPT_NUMBER,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_QUANTITY,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_TYPE,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_DISCOUNT,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_DISCOUNT_TYPE,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_PAYMENT_TYPE,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_PAYMENT,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_TOTAL,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_CHANGE,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_BY,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT
        };

        String selection = DatabaseHelper.COLUMN_RECEIPT_ORDER_NUMBER + " = ?";
        String[] selectionArgs = { "" + orderNumber + "" }; // Modify this according to your category value

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_RECEIPT,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        generateReceiptNumber(db, true);

        return cursor;
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

    @SuppressLint("Range")
    public int generateReceiptNumber(SQLiteDatabase db, boolean isCheckoutButtonClicked) {
        // Retrieve the last order number from the table
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_LAST_RECEIPT_NUMBER + " FROM " + TABLE_RECEIPT_NUMBER + " ORDER BY " + COLUMN_LAST_RECEIPT_NUMBER + " DESC LIMIT 1", null);

        int receiptNumber = 0;

        if (cursor.moveToFirst()) {
            receiptNumber = cursor.getInt(cursor.getColumnIndex(COLUMN_LAST_RECEIPT_NUMBER));
        }

        cursor.close();

        // Increment the order number for the next order only if the checkout button is clicked
        if (isCheckoutButtonClicked) {
            int nextOrderNumber = receiptNumber + 1;

            // Update the order number in the table or insert a new record if the table is empty
            ContentValues updateValues = new ContentValues();
            updateValues.put(COLUMN_LAST_RECEIPT_NUMBER, nextOrderNumber);

            if (receiptNumber == 0) {
                db.insert(TABLE_RECEIPT_NUMBER, null, updateValues);
            } else {
                db.update(TABLE_RECEIPT_NUMBER, updateValues, null, null);
            }
        }

        return receiptNumber;
    }


    public int orderNumber (){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int orderNumber;
        orderNumber = generateOrderNumber(db,false);
        return orderNumber;
    }

    public int receiptNumber (){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int receiptNumber;
        receiptNumber = generateReceiptNumber(db,false);
        return receiptNumber;
    }

    public double getPendingTotal(String pendingNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_PENDING_ORDER_TOTAL
        };

        String selection = DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER + " = ?";
        String[] selectionArgs = { pendingNumber };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PENDING_ORDERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        double total = 0.0;

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") double pendingTotal = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_PENDING_ORDER_TOTAL));
                total += pendingTotal;
            } while (cursor.moveToNext());
        }

        cursor.close();

        return total;
    }

    public Cursor getPendingOrders(int orderNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_PENDING_ORDER_ID,
                DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER,
                DatabaseHelper.COLUMN_PENDING_ORDER_NAME,
                DatabaseHelper.COLUMN_PENDING_ORDER_QUANTITY,
                DatabaseHelper.COLUMN_PENDING_ORDER_TYPE,
                DatabaseHelper.COLUMN_PENDING_ORDER_DISCOUNT,
                DatabaseHelper.COLUMN_PENDING_ORDER_DISCOUNT_TYPE,
                DatabaseHelper.COLUMN_PENDING_ORDER_PAYMENT_TYPE,
                DatabaseHelper.COLUMN_PENDING_ORDER_PAYMENT,
                DatabaseHelper.COLUMN_PENDING_ORDER_TOTAL,
                DatabaseHelper.COLUMN_PENDING_ORDER_CHANGE,
                DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_BY,
                DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_AT
        };

        String selection = DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER + " = ?";
        String[] selectionArgs = { "" + orderNumber + "" }; // Modify this according to your category value

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PENDING_ORDERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getOrders(int orderNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_ORDER_ID,
                DatabaseHelper.COLUMN_ORDER_NUMBER,
                DatabaseHelper.COLUMN_ORDER_NAME,
                DatabaseHelper.COLUMN_ORDER_QUANTITY,
                DatabaseHelper.COLUMN_ORDER_TYPE,
                DatabaseHelper.COLUMN_ORDER_DISCOUNT,
                DatabaseHelper.COLUMN_ORDER_DISCOUNT_TYPE,
                DatabaseHelper.COLUMN_ORDER_PAYMENT_TYPE,
                DatabaseHelper.COLUMN_ORDER_PAYMENT,
                DatabaseHelper.COLUMN_ORDER_TOTAL,
                DatabaseHelper.COLUMN_ORDER_CHANGE,
                DatabaseHelper.COLUMN_ORDER_ADDED_BY
        };

        String selection = DatabaseHelper.COLUMN_ORDER_NUMBER + " = ?";
        String[] selectionArgs = { "" + orderNumber + "" }; // Modify this according to your category value

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ORDERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }

    public Map<String, Double> getPendingOrderPrices(int orderNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_PENDING_ORDER_NAME,
                DatabaseHelper.COLUMN_PENDING_ORDER_TOTAL
        };

        String selection = DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER + " = ?";
        String[] selectionArgs = {String.valueOf(orderNumber)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PENDING_ORDERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Map<String, Double> itemPrices = new HashMap<>();

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PENDING_ORDER_NAME));
            @SuppressLint("Range") double itemPrice = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_PENDING_ORDER_TOTAL));

            if (itemPrices.containsKey(itemName)) {
                double currentPrice = itemPrices.get(itemName);
                itemPrices.put(itemName, currentPrice + itemPrice);
            } else {
                itemPrices.put(itemName, itemPrice);
            }
        }

        cursor.close();

        return itemPrices;
    }

    public Map<String, Double> getOrdersPrices(int orderNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_ORDER_NAME,
                DatabaseHelper.COLUMN_ORDER_TOTAL
        };

        String selection = DatabaseHelper.COLUMN_ORDER_NUMBER + " = ?";
        String[] selectionArgs = {String.valueOf(orderNumber)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ORDERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Map<String, Double> itemPrices = new HashMap<>();

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_NAME));
            @SuppressLint("Range") double itemPrice = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_TOTAL));

            if (itemPrices.containsKey(itemName)) {
                double currentPrice = itemPrices.get(itemName);
                itemPrices.put(itemName, currentPrice + itemPrice);
            } else {
                itemPrices.put(itemName, itemPrice);
            }
        }

        cursor.close();

        Log.d("itemPrices", String.valueOf(itemPrices));

        return itemPrices;
    }

    public void clearOrders() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PENDING_ORDERS, null, null);
        db.close();
    }

    public long insertGCash(String reference, String receiver_name, String receiver_number, double received_amount, String date_sent, String whole_message
            ,String transactionNumber, String OrderNumber, String username ,Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if a user with the same name already exists (case-insensitive comparison)
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_GCASH,
                null,
                COLUMN_GCASH_REFERENCE_NUMBER + " COLLATE NOCASE = ?",
                new String[]{reference},
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
        values.put(DatabaseHelper.COLUMN_GCASH_REFERENCE_NUMBER, reference);
        values.put(DatabaseHelper.COLUMN_GCASH_RECEIPT_NUMBER, transactionNumber);
        values.put(DatabaseHelper.COLUMN_GCASH_ORDER_NUMBER, OrderNumber);
        values.put(DatabaseHelper.COLUMN_GCASH_SENDER_NAME, receiver_name);
        values.put(DatabaseHelper.COLUMN_GCASH_SENDER_NUMBER, receiver_number);
        values.put(DatabaseHelper.COLUMN_GCASH_AMOUNT_RECEIVED, received_amount);
        values.put(DatabaseHelper.COLUMN_GCASH_WHOLE_MESSAGE, whole_message);
        values.put(DatabaseHelper.COLUMN_GCASH_DATE_SENT, date_sent);
        values.put(DatabaseHelper.COLUMN_GCASH_COUNTER_RECEIVER, username);

        long newRowId = db.insert(DatabaseHelper.TABLE_GCASH, null, values);

        cursor.close();
        db.close();

        return newRowId;
    }


}
