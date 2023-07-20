package com.example.pos;

import static com.example.pos.DatabaseHelper.COLUMN_GCASH_REFERENCE_NUMBER;
import static com.example.pos.DatabaseHelper.COLUMN_LAST_DTI_NUMBER;
import static com.example.pos.DatabaseHelper.COLUMN_LAST_ORDER_NUMBER;
import static com.example.pos.DatabaseHelper.COLUMN_LAST_RECEIPT_NUMBER;
import static com.example.pos.DatabaseHelper.COLUMN_MENU_ADDED_BY;
import static com.example.pos.DatabaseHelper.COLUMN_MENU_CATEGORY;
import static com.example.pos.DatabaseHelper.COLUMN_MENU_NAME;
import static com.example.pos.DatabaseHelper.COLUMN_MENU_PRICE;
import static com.example.pos.DatabaseHelper.COLUMN_MENU_UPDATED_AT;
import static com.example.pos.DatabaseHelper.COLUMN_MENU_UPDATED_BY;
import static com.example.pos.DatabaseHelper.COLUMN_ORDER_NUMBER;
import static com.example.pos.DatabaseHelper.COLUMN_OR_NUMBER;
import static com.example.pos.DatabaseHelper.COLUMN_QR_TEXT;
import static com.example.pos.DatabaseHelper.TABLE_DTI;
import static com.example.pos.DatabaseHelper.TABLE_MENU;
import static com.example.pos.DatabaseHelper.TABLE_ORDER_NUMBER;
import static com.example.pos.DatabaseHelper.TABLE_OR_NUMBER;
import static com.example.pos.DatabaseHelper.TABLE_QR;
import static com.example.pos.DatabaseHelper.TABLE_RECEIPT_NUMBER;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataAccess {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DataAccess(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
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

        loginUser(name, password);

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
        String[] selectionArgs = {"Staff"}; // Modify this according to your category value

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
        String[] selectionArgs = {"Admin"}; // Modify this according to your category value

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

    public void deleteUser(String itemName, Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_USER_NAME + " = ?";
        String[] whereArgs = { itemName };

        db.delete(DatabaseHelper.TABLE_USERS, whereClause, whereArgs);

        // For example, you can display a toast message indicating successful deletion
        Toast.makeText(context, "User '" + itemName + "' deleted", Toast.LENGTH_SHORT).show();
    }

    public void deleteMenu(String itemName, Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_MENU_NAME + " = ?";
        String[] whereArgs = { itemName };

        db.delete(DatabaseHelper.TABLE_MENU, whereClause, whereArgs);

        // For example, you can display a toast message indicating successful deletion
        Toast.makeText(context, "Menu '" + itemName + "' deleted", Toast.LENGTH_SHORT).show();
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

    public List<String> getUniqueUserNames() {
        List<String> uniqueItemNames = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT " + DatabaseHelper.COLUMN_USER_NAME + " FROM " + DatabaseHelper.TABLE_USERS;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME));
                uniqueItemNames.add(itemName);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return uniqueItemNames;
    }

    public Cursor getDisposableItems() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_ITEM_ID,
                DatabaseHelper.COLUMN_ITEM_NAME,
                DatabaseHelper.COLUMN_ITEM_DATE_IN
        };

        String selection = DatabaseHelper.COLUMN_ITEM_CATEGORY + " = ?";
        String[] selectionArgs = {"Disposables"}; // Modify this according to your category value

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
        String[] selectionArgs = {"Beverages"}; // Modify this according to your category value

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
        String[] selectionArgs = {"Food"}; // Modify this according to your category value

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
        String[] selectionArgs = {"Ingredients"}; // Modify this according to your category value

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
        String[] selectionArgs = {"Pastries"}; // Modify this according to your category value

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

    @SuppressLint("MissingPermission")
    public void getAllItems(Context context) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                "COUNT(*) AS count",
                DatabaseHelper.COLUMN_ITEM_NAME
        };

        Cursor cursor = db.query(
                true, // Set 'distinct' to true to get unique item names
                DatabaseHelper.TABLE_INVENTORY,
                projection,
                null, // No specific selection
                null, // No specific selection arguments
                DatabaseHelper.COLUMN_ITEM_NAME,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            int columnIndexCount = cursor.getColumnIndex("count");
            int columnIndexName = cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_NAME);

            do {
                int itemCount = cursor.getInt(columnIndexCount);
                String itemName = cursor.getString(columnIndexName);

                if (itemCount <= 2) {
                    int notificationId = itemName.hashCode(); // Use the item name's hashcode as the notification ID

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                            .setSmallIcon(R.drawable.warning_icon)
                            .setContentTitle("Low Item Count")
                            .setContentText("Only " + itemCount + " " + itemName + " remaining!")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setDefaults(NotificationCompat.DEFAULT_ALL); // Enable default sound and vibration

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                    // Create a notification channel (required for Android 8.0 and above)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CharSequence channelName = "Notification Channel";
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel("channel_id", channelName, importance);
                        notificationManager.createNotificationChannel(channel);
                    }

                    notificationManager.notify(notificationId, builder.build());
                }
            } while (cursor.moveToNext());
        }

        cursor.close(); // Close the cursor after processing

        // Optionally, you can return the cursor if you need to use it for further operations
        // return cursor;
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

    public long insertPendingOrders(String name, int quantity, String type, double discount, String discountType, String paymentType, double payment, double total, double change, String notes, String addedBy) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();


        String productName;
        if (type.contains("Hot")){
            productName = "Hot " + name;
        }else if (type.contains("Cold")){
            productName = "Cold " + name;
        }else{
            productName = name;
        }

        // Insert the pending orders
        long newRowId = -1;
        if (quantity > 0) {
            db.beginTransaction();
            try {
                for (int i = 0; i < quantity; i++) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER, generateOrderNumber(db, false)); // You need to implement the generateOrderNumber() method with the database parameter
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_NAME, productName);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_QUANTITY, 1); // Set the quantity to 1 for each row
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_CATEGORY, type);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_DISCOUNT, discount);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_DISCOUNT_TYPE, discountType);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_PAYMENT_TYPE, paymentType);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_PAYMENT, payment);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_TOTAL, total);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_CHANGE, change);
                    values.put(DatabaseHelper.COLUMN_PENDING_ORDER_NOTE, notes);
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
    public long insertReceipt(int orderID, String addedBy, Context context) {
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

        if (!orderCursor.moveToFirst()) {
            // Empty cursor, no data found
            orderCursor.close();
            return -1;
        }

        String firstDiscountType = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_DISCOUNT_TYPE));
        String differentDiscountType = null;
        boolean allDiscountsNoDiscount = true;

        while (orderCursor.moveToNext()) {
            String discountType = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_DISCOUNT_TYPE));

            if (!discountType.equals(firstDiscountType)) {
                differentDiscountType = discountType;
                allDiscountsNoDiscount = false;
                break; // Exit the loop after finding the first different discount type
            }
        }

        // Assign the different discount type to orderDiscount if available, otherwise set it to "No Discount"
        if (differentDiscountType != null) {
            orderDiscountType = differentDiscountType;
        } else if (allDiscountsNoDiscount) {
            orderDiscountType = firstDiscountType;
        }


        // Rest of your code
        orderCursor.moveToFirst();
        orderType = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_TYPE));
        orderDiscount = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_DISCOUNT));
        orderPaymentType = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_PAYMENT_TYPE));
        orderPayment = orderCursor.getDouble(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_PAYMENT));
        orderChange = orderCursor.getDouble(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_CHANGE));

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

    public Cursor getReceiptOnly(int orderNumber) {
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
                DatabaseHelper.COLUMN_ORDER_NOTE,
                DatabaseHelper.COLUMN_ORDER_ADDED_BY
        };

        String selection = DatabaseHelper.COLUMN_ORDER_NUMBER + " = ? ";
        String[] selectionArgs = { "" + orderNumber + ""}; // Modify this according to your category value

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
                DatabaseHelper.COLUMN_PENDING_ORDER_TOTAL,
                DatabaseHelper.COLUMN_PENDING_ORDER_CATEGORY
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

    public Cursor getTotalSales(String startDate, String endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                "strftime('%Y-%m-%d', " + DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT + ") AS " + DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT,
                "SUM(" + DatabaseHelper.COLUMN_RECEIPT_ORDER_TOTAL + ") AS " + DatabaseHelper.COLUMN_RECEIPT_ORDER_TOTAL,
                DatabaseHelper.COLUMN_RECEIPT_NUMBER,
                DatabaseHelper.COLUMN_RECEIPT_ORDER_NUMBER
        };


        String selection;
        String[] selectionArgs;
        String groupBy = "strftime('%Y-%m-%d', " + DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT + ")";

        if (startDate != null && endDate != null) {

                selection = DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT + " BETWEEN ? AND ?";
                selectionArgs = new String[]{startDate, endDate};
        } else {
                selection = null;
                selectionArgs = null;
        }

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_RECEIPT,
                projection,
                selection,
                selectionArgs,
                groupBy,
                null,
                null
        );

        return cursor;
    }

    public Cursor getListReceipts(String startDate, String endDate) {
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

        String selection = null;
        List<String> selectionArgsList = new ArrayList<>();

        if (startDate != null && endDate != null) {
            selection = DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT + " BETWEEN ? AND ?";
            selectionArgsList.add(startDate);
            selectionArgsList.add(endDate);
        }

        String[] selectionArgs = null;
        if (!selectionArgsList.isEmpty()) {
            selectionArgs = selectionArgsList.toArray(new String[0]);
        }

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_RECEIPT,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getCashDrawer(String dateEntered) {
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

        String selection = null;
        String[] selectionArgs = null;

        if (dateEntered != null) {
            // Convert the dateEntered to the appropriate format
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedDateEntered = LocalDate.parse(dateEntered, inputFormatter);

            // Create start and end timestamps for the selected date
            LocalDateTime startDateTime = parsedDateEntered.atStartOfDay();
            LocalDateTime endDateTime = parsedDateEntered.atTime(LocalTime.MAX);

            // Convert the timestamps to formatted strings
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            String formattedStartDate = startDateTime.format(outputFormatter);
            String formattedEndDate = endDateTime.format(outputFormatter);

            // Set the selection and selectionArgs
            selection = DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT + " BETWEEN ? AND ?";
            selectionArgs = new String[]{formattedStartDate, formattedEndDate};
        }else {
            // Get the current date and time
            LocalDateTime currentDateTime = LocalDateTime.now();

            // Convert the current date to a formatted string
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedCurrentDate = currentDateTime.format(outputFormatter);

            // Set the selection and selectionArgs to query for the current date
            selection = DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT + " >= ?";
            selectionArgs = new String[]{formattedCurrentDate};
        }

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_RECEIPT,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }



    public Map<String, Integer> getOrdersPieChartDaily(String category, String startDate, String endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Log.d("PieData", "category: " + category + ", startDate: " + startDate + ", endDate: " + endDate);

        String[] projection = {
                "DISTINCT " + DatabaseHelper.COLUMN_ORDER_NAME,
                "COUNT(" + DatabaseHelper.COLUMN_ORDER_NAME + ") AS nameCount"
        };

        String groupBy = DatabaseHelper.COLUMN_ORDER_NAME;

        String selection = null;
        List<String> selectionArgsList = new ArrayList<>();

        if (category.equalsIgnoreCase("Overall")) {
            // No additional selection criteria
        } else if (category.equalsIgnoreCase("Espresso") || category.equalsIgnoreCase("Non Espresso")) {
            selection = DatabaseHelper.COLUMN_CATEGORY + "=? OR " + DatabaseHelper.COLUMN_CATEGORY + "=?";
            selectionArgsList.add("Hot " + category);
            selectionArgsList.add("Cold " + category);
        } else {
            selection = DatabaseHelper.COLUMN_CATEGORY + "=?";
            selectionArgsList.add(category);
        }

        if (startDate != null && endDate != null) {
            // Convert the start date and end date to the appropriate format
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            try {
                LocalDate parsedStartDate = LocalDate.parse(startDate, inputFormatter);
                LocalDate parsedEndDate = LocalDate.parse(endDate, inputFormatter);

                LocalDateTime startDateTime = parsedStartDate.atStartOfDay();
                LocalDateTime endDateTime = parsedEndDate.atTime(LocalTime.MAX);

                String formattedStartDate = startDateTime.format(outputFormatter);
                String formattedEndDate = endDateTime.format(outputFormatter);

                Log.d("Formatted Date", formattedStartDate + ", " + formattedEndDate);

                if (selection != null) {
                    selection += " AND ";
                } else {
                    selection = "";
                }
                selection += DatabaseHelper.COLUMN_ORDER_ADDED_AT + " BETWEEN ? AND ?";
                selectionArgsList.add(formattedStartDate);
                selectionArgsList.add(formattedEndDate);
            } catch (DateTimeParseException e) {
                // Handle the exception or log an error message
                e.printStackTrace();
            }
        }

        String[] selectionArgs = selectionArgsList.toArray(new String[0]);

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ORDERS,
                projection,
                selection,
                selectionArgs,
                groupBy,
                null,
                null
        );

        Map<String, Integer> nameCounts = new LinkedHashMap<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_NAME));
                @SuppressLint("Range") int count = cursor.getInt(cursor.getColumnIndex("nameCount"));

                nameCounts.put(itemName, count);
            } while (cursor.moveToNext());

            cursor.close();
        }

        // Sort the nameCounts map by value in descending order
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(nameCounts.entrySet());
        sortedList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        Map<String, Integer> sortedNameCounts = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : sortedList) {
            sortedNameCounts.put(entry.getKey(), entry.getValue());
        }

        Log.d("PieData", "sortedNameCounts: " + sortedNameCounts);

        return sortedNameCounts;
    }


    public long insertMovement(int userId, String userName, String movement, String addedAt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TRANSACTION_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_TRANSACTION_USER_NAME, userName);
        values.put(DatabaseHelper.COLUMN_TRANSACTION_MOVEMENT, movement);
        values.put(DatabaseHelper.COLUMN_TRANSACTION_ADDED_AT, addedAt);

        long newRowId = db.insert(DatabaseHelper.TABLE_TRANSACTION, null, values);

        db.close();

        return newRowId;
    }

    public Cursor getTransactions(String dateEntered) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_TRANSACTION_ID,
                DatabaseHelper.COLUMN_TRANSACTION_USER_ID,
                DatabaseHelper.COLUMN_TRANSACTION_USER_NAME,
                DatabaseHelper.COLUMN_TRANSACTION_MOVEMENT,
                DatabaseHelper.COLUMN_TRANSACTION_ADDED_AT
        };

        String selection = null;
        String[] selectionArgs = null;

        if (dateEntered != null) {
            // Convert the dateEntered to the appropriate format
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedDateEntered = LocalDate.parse(dateEntered, inputFormatter);

            // Create start and end timestamps for the selected date
            LocalDateTime startDateTime = parsedDateEntered.atStartOfDay();
            LocalDateTime endDateTime = parsedDateEntered.atTime(LocalTime.MAX);

            // Convert the timestamps to formatted strings
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            String formattedStartDate = startDateTime.format(outputFormatter);
            String formattedEndDate = endDateTime.format(outputFormatter);

            // Set the selection and selectionArgs
            selection = DatabaseHelper.COLUMN_TRANSACTION_ADDED_AT + " BETWEEN ? AND ?";
            selectionArgs = new String[]{formattedStartDate, formattedEndDate};
        } else {
            // Get the current date and time
            LocalDateTime currentDateTime = LocalDateTime.now();

            // Convert the current date to a formatted string
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedCurrentDate = currentDateTime.format(outputFormatter);

            // Set the selection and selectionArgs to query for the current date
            selection = DatabaseHelper.COLUMN_TRANSACTION_ADDED_AT + " >= ?";
            selectionArgs = new String[]{formattedCurrentDate};
        }

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_TRANSACTION,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        return cursor;
    }


    public int updateUser(String userName, String logUser, String fullName, String password, String conpassword, int userID, Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if a user with the same name already exists (case-insensitive comparison)
        Cursor cursorCheck = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USER_NAME + " COLLATE NOCASE = ?",
                new String[]{userName},
                null,
                null,
                null
        );

        // Check if the cursor has any rows, indicating that a user with the same name already exists
        if (cursorCheck.getCount() > 0) {
            cursorCheck.close();
            db.close();
            return -1; // Return -1 to indicate that the insertion failed due to a duplicate user name
        }

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

        if (values.size() > 0) {
            String selection = DatabaseHelper.COLUMN_USER_NAME + " = ?";
            String[] selectionArgs = {String.valueOf(logUser)};

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
        return userID;
    }

    public int updateBusiness(String birNumber, String dtiNumber, String orderNumber, String qrText,String receiptNumber, Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        if (birNumber != null && !birNumber.isEmpty()) {
            ContentValues birNumberValues = new ContentValues();
            birNumberValues.put(COLUMN_OR_NUMBER, birNumber);
            rowsAffected += db.update(TABLE_OR_NUMBER, birNumberValues, null, null);
        }

        if (dtiNumber != null && !dtiNumber.isEmpty()) {
            ContentValues dtiValues = new ContentValues();
            dtiValues.put(COLUMN_LAST_DTI_NUMBER, dtiNumber);
            rowsAffected += db.update(TABLE_DTI, dtiValues, null, null);
        }

        if (orderNumber != null && !orderNumber.isEmpty()) {
            ContentValues orderValues = new ContentValues();
            orderValues.put(COLUMN_LAST_ORDER_NUMBER, orderNumber);
            rowsAffected += db.update(TABLE_ORDER_NUMBER, orderValues, null, null);
        }

        if (qrText != null && !qrText.isEmpty()) {
            ContentValues qrValues = new ContentValues();
            qrValues.put(COLUMN_QR_TEXT, qrText);
            rowsAffected += db.update(TABLE_QR, qrValues, null, null);
        }

        if (receiptNumber != null && !receiptNumber.isEmpty()) {
            ContentValues receiptValues = new ContentValues();
            receiptValues.put(COLUMN_LAST_RECEIPT_NUMBER, receiptNumber);
            rowsAffected += db.update(TABLE_RECEIPT_NUMBER, receiptValues, null, null);
        }

        db.close();
        Log.d("ROWS UPDATE: ", String.valueOf(rowsAffected) + birNumber + dtiNumber + orderNumber + qrText);
        return rowsAffected;
    }


    public Cursor getUserAccount(String userName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        String[] projection = {
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_USER_NAME,
                DatabaseHelper.COLUMN_FUll_NAME,
                DatabaseHelper.COLUMN_CATEGORY,
                DatabaseHelper.COLUMN_STATUS,
                DatabaseHelper.COLUMN_USER_CREATED_AT
        };

        String selection = DatabaseHelper.COLUMN_USER_NAME + " = ?";
        String[] selectionArgs = {userName}; // Modify this according to your category value

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

    public Cursor getOrderNumber() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_LAST_ORDER_NUMBER,
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ORDER_NUMBER,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getOR() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_OR_NUMBER,
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_OR_NUMBER,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getReceiptNumber() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_LAST_RECEIPT_NUMBER,
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_RECEIPT_NUMBER,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getDTINumber() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_LAST_DTI_NUMBER,
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_DTI,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        return cursor;
    }

    public Cursor getQRCode() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_QR_TEXT,
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_QR,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        return cursor;
    }

    public boolean getDiscountLimit(int orderNumber, String orderType) {
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
                DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_AT,
                DatabaseHelper.COLUMN_PENDING_ORDER_CATEGORY
        };

        String selection;
        String[] selectionArgs;

        if (!orderType.equals("Short Orders")) {
            selection = DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER + " = ? AND " +
                    DatabaseHelper.COLUMN_PENDING_ORDER_CATEGORY + " != ? AND " +
                    DatabaseHelper.COLUMN_PENDING_ORDER_DISCOUNT_TYPE + " != ?";
            selectionArgs = new String[]{String.valueOf(orderNumber), "Short Orders", "No Discount"};
        } else {
            selection = DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER + " = ? AND " +
                    DatabaseHelper.COLUMN_PENDING_ORDER_CATEGORY + " = ? AND " +
                    DatabaseHelper.COLUMN_PENDING_ORDER_DISCOUNT_TYPE + " != ?";
            selectionArgs = new String[]{String.valueOf(orderNumber), "Short Orders", "No Discount"};
        }

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PENDING_ORDERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean hasDiscount = cursor.getCount() > 0;

        cursor.close();

        Log.d("Discount", orderType + " " + orderNumber + " " + hasDiscount);

        return hasDiscount;
    }



    public Cursor getPendingDiscounted(int orderNumber) {
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
                DatabaseHelper.COLUMN_PENDING_ORDER_ADDED_AT,
                DatabaseHelper.COLUMN_PENDING_ORDER_CATEGORY
        };

        String selection = DatabaseHelper.COLUMN_PENDING_ORDER_NUMBER + " = ?";
        String[] selectionArgs = { String.valueOf(orderNumber) };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PENDING_ORDERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            boolean hasDiscountedDrink = false;
            boolean hasDiscountedFood = false;

            do {
                @SuppressLint("Range") String discountType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PENDING_ORDER_DISCOUNT_TYPE));
                @SuppressLint("Range") String itemType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PENDING_ORDER_CATEGORY));

                Log.d("Item Type: ", itemType);

                if (!discountType.equals("No Discount")) {
                    if (itemType.equals("Short Orders")) {
                        // Discounted food item
                        if (hasDiscountedFood || hasDiscountedDrink) {
                            cursor.close();
                            return null;
                        }
                        hasDiscountedFood = true;
                    } else {
                        // Discounted drink
                        if (hasDiscountedDrink || hasDiscountedFood) {
                            cursor.close();
                            return null;
                        }
                        hasDiscountedDrink = true;
                    }
                }
            } while (cursor.moveToNext());

            if (hasDiscountedDrink || hasDiscountedFood) {
                // More than one discounted drink or food item, order is invalid
                cursor.close();
                return null;
            }
        }

        return cursor;
    }

    public long insertDefaultMenu(Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int numRowsAdded = 0; // Initialize newRowId with a default value in case of an error.
        long newRowId = -1;
        LocalDateTime currentDateTime = LocalDateTime.now();
        String date_in = currentDateTime.toString();

        try {
            ContentValues values = new ContentValues();
            String[][] menuData = {
                    {"Americano", "H Espresso", "120"},
                    {"Cappuccino", "H Espresso", "140"},
                    {"Cafe Latte", "H Espresso", "150"},
                    {"Mocha Latte", "H Espresso", "160"},
                    {"White Chocolate", "H Espresso", "160"},
                    {"Caramel Macchiato", "H Espresso", "160"},
                    {"Salted Caramel", "H Espresso", "160"},
                    {"Spanish Latte", "H Espresso", "170"},
                    {"Hazelnut Latte", "H Espresso", "170"},
                    {"Dirty Matcha", "H Espresso", "180"},
                    {"Oreo Cream Latte", "H Espresso", "180"},
                    {"SMAK Latte", "H Espresso", "190"},
                    {"Strawberry", "Fruit Tea", "150"},
                    {"Passion Fruit", "Fruit Tea", "150"},
                    {"Green Apple", "Fruit Tea", "150"},
                    {"Lychee", "Fruit Tea", "160"},
                    {"Mango", "Fruit Tea", "160"},
                    {"Matcha", "H Non Espresso", "150"},
                    {"Strawberry", "H Non Espresso", "150"},
                    {"Milk Milky", "H Non Espresso", "150"},
                    {"Chocolate", "H Non Espresso", "150"},
                    {"Milky Caramel", "H Non Espresso", "150"},
                    {"Strawberry", "Frappe", "180"},
                    {"Matcha", "Frappe", "180"},
                    {"Oreo Matcha", "Frappe", "190"},
                    {"Coffee Jelly", "Frappe", "190"},
                    {"Choco Caramel", "Frappe", "190"},
                    {"Caramel", "Frappe", "190"},
                    {"White Chocolate", "Frappe", "170"},
                    {"Bellagio Chocolate", "Frappe", "180"},
                    {"Cookies & Cream", "Frappe", "180"},
                    {"Strawberry", "Sparkling Ade", "150"},
                    {"Passion Fruit", "Sparkling Ade", "150"},
                    {"Blueberry", "Sparkling Ade", "150"},
                    {"Green Apple", "Sparkling Ade", "150"},
                    {"Nata", "Add Ons", "20"},
                    {"Coffee Jelly", "Add Ons", "20"},
                    {"Extra Shot", "Add Ons", "40"}
                    // Add more rows here in the same format
            };

            for (String[] menu : menuData) {
                String menuName = menu[0];
                String menuCategory = menu[1];
                String menuPrice = menu[2];

                // Check if the menu name already exists in the database
                Cursor cursor = db.query(TABLE_MENU,
                        new String[]{COLUMN_MENU_NAME},
                        COLUMN_MENU_NAME + "=?",
                        new String[]{menuName},
                        null,
                        null,
                        null);

                if (cursor.getCount() == 0) {
                    // The menu name is unique, so insert the row
                    values.put(COLUMN_MENU_NAME, menuName);
                    values.put(COLUMN_MENU_CATEGORY, menuCategory);
                    values.put(COLUMN_MENU_PRICE, menuPrice);
                    values.put(COLUMN_MENU_ADDED_BY, (String) null);
                    values.put(COLUMN_MENU_UPDATED_BY, (String) null);
                    values.put(COLUMN_MENU_UPDATED_AT, date_in);

                    newRowId = db.insert(TABLE_MENU, null, values);
                    values.clear();

                    if (newRowId != -1) {
                        // Row inserted successfully, increment the counter
                        numRowsAdded++;
                    }
                }

                cursor.close();
            }

            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "An error occurred while adding default menu.", Toast.LENGTH_SHORT).show();
            return newRowId;
        }
        
        if (numRowsAdded > 0) {
            Toast.makeText(context, numRowsAdded + " rows added to the menu.", Toast.LENGTH_SHORT).show();
            return newRowId;
        } else {
            Toast.makeText(context, "Nothing was changed. Menu already contains the default items.", Toast.LENGTH_SHORT).show();
            return newRowId;
        }
    }











}



