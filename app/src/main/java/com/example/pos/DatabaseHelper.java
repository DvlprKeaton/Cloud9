package com.example.pos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pos.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_TRANSACTION = "transactions";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_INVENTORY = "inventory";

    // Transaction table columns
    public static final String COLUMN_TRANSACTION_ID = "id";
    public static final String COLUMN_TRANSACTION_AMOUNT = "amount";
    // Add more transaction columns as needed

    // Users table columns
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_FUll_NAME = "name";
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_USER_CREATED_AT = "created_at";
    public static final String COLUMN_USER_UPDATED_AT = "updated_at";
    // Add more user columns as needed

    // Inventory table columns
    public static final String COLUMN_ITEM_ID = "id";
    public static final String COLUMN_ITEM_NAME = "name";
    public static final String COLUMN_ITEM_CATEGORY = "category";
    public static final String COLUMN_ITEM_DATE_IN = "date_in";
    public static final String COLUMN_ITEM_DATE_OUT = "date_out";
    public static final String COLUMN_ITEM_ADDED_BY = "added_by";
    public static final String COLUMN_ITEM_UPDATED_BY = "updated_by";
    public static final String COLUMN_ITEM_UPDATED_AT = "updated_at";
    // Add more user columns as needed

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the transaction table
        String createTransactionTable = "CREATE TABLE " + TABLE_TRANSACTION + " (" +
                COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRANSACTION_AMOUNT + " REAL NOT NULL)";
        db.execSQL(createTransactionTable);

        // Create the users table
        String createUserTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_NAME + " TEXT NOT NULL, "+
                COLUMN_FUll_NAME + " TEXT NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_STATUS + " TEXT NOT NULL, "+
                COLUMN_USER_CREATED_AT + " TEXT NOT NULL, "+
                COLUMN_USER_UPDATED_AT + " TEXT)";
        db.execSQL(createUserTable);

        // Create the inventory table
        String createInventoryTable = "CREATE TABLE " + TABLE_INVENTORY + " (" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                COLUMN_ITEM_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_ITEM_DATE_IN + " TEXT, " +
                COLUMN_ITEM_DATE_OUT + " TEXT, " +
                COLUMN_ITEM_ADDED_BY + " TEXT," +
                COLUMN_ITEM_UPDATED_BY + " TEXT,"+
                COLUMN_ITEM_UPDATED_AT + " TEXT)";
        db.execSQL(createInventoryTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

}
