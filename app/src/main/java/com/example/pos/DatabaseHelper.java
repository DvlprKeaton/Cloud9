package com.example.pos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pos.db";
    private static final int DATABASE_VERSION = 4;

    // Table names
    public static final String TABLE_TRANSACTION = "transactions";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_INVENTORY = "inventory";
    public static final String TABLE_MENU = "menu";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_PENDING_ORDERS = "pending_orders";
    public static final String TABLE_ORDER_NUMBER = "order_numbers";
    public static final String TABLE_RECEIPT = "receipt";
    public static final String TABLE_RECEIPT_NUMBER = "receipt_number";
    public static final String TABLE_GCASH = "gcash";
    public static final String TABLE_DTI = "dti";
    public static final String TABLE_QR = "qr";
    public static final String TABLE_OR_NUMBER = "or_number";



    // Transaction table columns
    public static final String COLUMN_TRANSACTION_ID = "id";
    public static final String COLUMN_TRANSACTION_USER_ID = "user_id";
    public static final String COLUMN_TRANSACTION_USER_NAME = "user_name";
    public static final String COLUMN_TRANSACTION_MOVEMENT = "movement";
    public static final String COLUMN_TRANSACTION_ADDED_AT = "added_at";
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

    // Menu table columns
    public static final String COLUMN_MENU_ID = "id";
    public static final String COLUMN_MENU_NAME = "name";
    public static final String COLUMN_MENU_CATEGORY = "category";
    public static final String COLUMN_MENU_PRICE = "price";
    public static final String COLUMN_MENU_ADDED_BY = "added_by";
    public static final String COLUMN_MENU_UPDATED_BY = "updated_by";
    public static final String COLUMN_MENU_UPDATED_AT = "updated_at";
    // Add more user columns as needed

    // Orders table columns
    public static final String COLUMN_ORDER_ID = "id";
    public static final String COLUMN_ORDER_NUMBER= "order_number";
    public static final String COLUMN_ORDER_NAME = "name";
    public static final String COLUMN_ORDER_QUANTITY = "quantity";
    public static final String COLUMN_ORDER_CATEGORY = "category";
    public static final String COLUMN_ORDER_TYPE = "type";
    public static final String COLUMN_ORDER_DISCOUNT = "discount";
    public static final String COLUMN_ORDER_DISCOUNT_TYPE = "discount_type";
    public static final String COLUMN_ORDER_PAYMENT_TYPE = "payment_type";
    public static final String COLUMN_ORDER_PAYMENT= "payment";
    public static final String COLUMN_ORDER_TOTAL = "total";
    public static final String COLUMN_ORDER_CHANGE = "change";
    public static final String COLUMN_ORDER_NOTE = "note";
    public static final String COLUMN_ORDER_ADDED_BY = "added_by";
    public static final String COLUMN_ORDER_ADDED_AT = "added_at";
    // Add more user columns as needed

    // Pending Orders table columns
    public static final String COLUMN_PENDING_ORDER_ID = "id";
    public static final String COLUMN_PENDING_ORDER_NUMBER= "order_number";
    public static final String COLUMN_PENDING_ORDER_NAME = "name";
    public static final String COLUMN_PENDING_ORDER_QUANTITY = "quantity";
    public static final String COLUMN_PENDING_ORDER_CATEGORY = "category";
    public static final String COLUMN_PENDING_ORDER_TYPE = "type";
    public static final String COLUMN_PENDING_ORDER_DISCOUNT = "discount";
    public static final String COLUMN_PENDING_ORDER_DISCOUNT_TYPE = "discount_type";
    public static final String COLUMN_PENDING_ORDER_PAYMENT_TYPE = "payment_type";
    public static final String COLUMN_PENDING_ORDER_PAYMENT= "payment";
    public static final String COLUMN_PENDING_ORDER_TOTAL = "total";
    public static final String COLUMN_PENDING_ORDER_CHANGE = "change";
    public static final String COLUMN_PENDING_ORDER_NOTE = "note";
    public static final String COLUMN_PENDING_ORDER_ADDED_BY = "added_by";
    public static final String COLUMN_PENDING_ORDER_ADDED_AT = "added_at";
    // Add more user columns as needed

    //Order Number table columns
    public static final String COLUMN_ORDER_NUMBER_ID = "id";
    public static final String COLUMN_LAST_ORDER_NUMBER = "last_order_number";

    // Pending Orders table columns
    public static final String COLUMN_RECEIPT_ID = "id";
    public static final String COLUMN_RECEIPT_ORDER_NUMBER= "order_number";
    public static final String COLUMN_RECEIPT_NUMBER = "receipt_number";
    public static final String COLUMN_RECEIPT_ORDER_QUANTITY = "order_quantity";
    public static final String COLUMN_RECEIPT_ORDER_TYPE = "type";
    public static final String COLUMN_RECEIPT_ORDER_DISCOUNT = "discount";
    public static final String COLUMN_RECEIPT_ORDER_DISCOUNT_TYPE = "discount_type";
    public static final String COLUMN_RECEIPT_ORDER_PAYMENT_TYPE = "payment_type";
    public static final String COLUMN_RECEIPT_ORDER_PAYMENT= "payment";
    public static final String COLUMN_RECEIPT_ORDER_TOTAL = "total";
    public static final String COLUMN_RECEIPT_ORDER_CHANGE = "change";
    public static final String COLUMN_RECEIPT_ORDER_ADDED_BY = "added_by";
    public static final String COLUMN_RECEIPT_ORDER_ADDED_AT = "added_at";

    //Order Number table columns
    public static final String COLUMN_RECEIPT_NUMBER_ID = "id";
    public static final String COLUMN_LAST_RECEIPT_NUMBER = "last_order_number";

    //Order Number table columns
    public static final String COLUMN_DTI_NUMBER_ID = "id";
    public static final String COLUMN_LAST_DTI_NUMBER = "dti_number";

    //Order Number table columns
    public static final String COLUMN_QR_NUMBER_ID = "id";
    public static final String COLUMN_QR_TEXT = "qr_text";

    //Order Number table columns
    public static final String COLUMN_OR_NUMBER_ID = "id";
    public static final String COLUMN_OR_NUMBER = "or_number";

    // Pending Orders table columns
    public static final String COLUMN_GCASH_ID = "id";
    public static final String COLUMN_GCASH_RECEIPT_NUMBER= "receipt_number";
    public static final String COLUMN_GCASH_ORDER_NUMBER= "order_number";
    public static final String COLUMN_GCASH_REFERENCE_NUMBER= "reference_number";
    public static final String COLUMN_GCASH_SENDER_NAME = "sender_name";
    public static final String COLUMN_GCASH_SENDER_NUMBER= "sender_number";
    public static final String COLUMN_GCASH_AMOUNT_RECEIVED = "amount_received";
    public static final String COLUMN_GCASH_WHOLE_MESSAGE = "whole_message";
    public static final String COLUMN_GCASH_DATE_SENT = "date_sent";
    public static final String COLUMN_GCASH_COUNTER_RECEIVER = "counter_receiver";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the transaction table
        String createTransactionTable = "CREATE TABLE " + TABLE_TRANSACTION + " (" +
                COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRANSACTION_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_TRANSACTION_USER_NAME + " TEXT NOT NULL, " +
                COLUMN_TRANSACTION_MOVEMENT + " TEXT NOT NULL, " +
                COLUMN_TRANSACTION_ADDED_AT + " TEXT NOT NULL)";
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

        // Create the menu table
        String createMenuTable = "CREATE TABLE " + TABLE_MENU + " (" +
                COLUMN_MENU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MENU_NAME + " TEXT NOT NULL, " +
                COLUMN_MENU_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_MENU_PRICE + " TEXT NOT NULL, " +
                COLUMN_MENU_ADDED_BY + " TEXT, " +
                COLUMN_MENU_UPDATED_BY + " TEXT, " +
                COLUMN_MENU_UPDATED_AT + " TEXT)";
        db.execSQL(createMenuTable);

        // Create the orders table
        String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + " (" +
                COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ORDER_NUMBER + " TEXT NOT NULL, " +
                COLUMN_ORDER_NAME + " TEXT NOT NULL, " +
                COLUMN_ORDER_QUANTITY + " INTEGER NOT NULL, " +
                COLUMN_ORDER_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_ORDER_TYPE + " TEXT NOT NULL, " +
                COLUMN_ORDER_DISCOUNT + " REAL NOT NULL, " +
                COLUMN_ORDER_DISCOUNT_TYPE + " TEXT NOT NULL, " +
                COLUMN_ORDER_PAYMENT_TYPE + " TEXT NOT NULL, " +
                COLUMN_ORDER_PAYMENT + " REAL NOT NULL, " +
                COLUMN_ORDER_TOTAL + " REAL NOT NULL, " +
                COLUMN_ORDER_CHANGE + " REAL NOT NULL, " +
                COLUMN_ORDER_NOTE + " TEXT, " +
                COLUMN_ORDER_ADDED_BY + " TEXT, " +
                COLUMN_ORDER_ADDED_AT + " TEXT)";
        db.execSQL(createOrdersTable);

    // Create the pending orders table
        String createPendingOrdersTable = "CREATE TABLE " + TABLE_PENDING_ORDERS + " (" +
                COLUMN_PENDING_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PENDING_ORDER_NUMBER + " TEXT NOT NULL, " +
                COLUMN_PENDING_ORDER_NAME + " TEXT NOT NULL, " +
                COLUMN_PENDING_ORDER_QUANTITY + " INTEGER NOT NULL, " +
                COLUMN_PENDING_ORDER_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_PENDING_ORDER_TYPE + " TEXT, " +
                COLUMN_PENDING_ORDER_DISCOUNT + " REAL NOT NULL, " +
                COLUMN_PENDING_ORDER_DISCOUNT_TYPE + " TEXT NOT NULL, " +
                COLUMN_PENDING_ORDER_PAYMENT_TYPE + " TEXT, " +
                COLUMN_PENDING_ORDER_PAYMENT + " REAL , " +
                COLUMN_PENDING_ORDER_TOTAL + " REAL , " +
                COLUMN_PENDING_ORDER_CHANGE + " REAL , " +
                COLUMN_PENDING_ORDER_NOTE + " TEXT, " +
                COLUMN_PENDING_ORDER_ADDED_BY + " TEXT," +
                COLUMN_PENDING_ORDER_ADDED_AT + " TEXT)";
        db.execSQL(createPendingOrdersTable);

        String createOrderNumberTable = "CREATE TABLE " + TABLE_ORDER_NUMBER + " (" +
                COLUMN_ORDER_NUMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LAST_ORDER_NUMBER + " INTEGER NOT NULL)";
        db.execSQL(createOrderNumberTable);

        // Insert the initial order number value
        ContentValues orderNumberValues = new ContentValues();
        orderNumberValues.put(COLUMN_LAST_ORDER_NUMBER, 1); // Set your desired initial order number
        db.insert(TABLE_ORDER_NUMBER, null, orderNumberValues);

        // Create the receipts table
        String createReceiptsTable = "CREATE TABLE " + TABLE_RECEIPT + " (" +
                COLUMN_RECEIPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECEIPT_ORDER_NUMBER + " TEXT NOT NULL, " +
                COLUMN_RECEIPT_NUMBER + " TEXT NOT NULL, " +
                COLUMN_RECEIPT_ORDER_QUANTITY + " INTEGER NOT NULL, " +
                COLUMN_RECEIPT_ORDER_TYPE + " TEXT NOT NULL, " +
                COLUMN_RECEIPT_ORDER_DISCOUNT + " REAL NOT NULL, " +
                COLUMN_RECEIPT_ORDER_DISCOUNT_TYPE + " TEXT NOT NULL, " +
                COLUMN_RECEIPT_ORDER_PAYMENT_TYPE + " TEXT, " +
                COLUMN_RECEIPT_ORDER_PAYMENT + " REAL, " +
                COLUMN_RECEIPT_ORDER_TOTAL + " REAL, " +
                COLUMN_RECEIPT_ORDER_CHANGE + " REAL, " +
                COLUMN_RECEIPT_ORDER_ADDED_BY + " TEXT, " +
                COLUMN_RECEIPT_ORDER_ADDED_AT + " TEXT)";
        db.execSQL(createReceiptsTable);


        String createReceiptNumberTable = "CREATE TABLE " + TABLE_RECEIPT_NUMBER + " (" +
                COLUMN_RECEIPT_NUMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LAST_RECEIPT_NUMBER + " INTEGER NOT NULL)";
        db.execSQL(createReceiptNumberTable);

        // Insert the initial order number value
        ContentValues receiptNumberValues = new ContentValues();
        receiptNumberValues.put(COLUMN_LAST_RECEIPT_NUMBER, 1); // Set your desired initial order number
        db.insert(TABLE_RECEIPT_NUMBER, null, receiptNumberValues);

        // Create the pending GCash orders table
        String createPendingGCashOrdersTable = "CREATE TABLE " + TABLE_GCASH + " (" +
                COLUMN_GCASH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GCASH_RECEIPT_NUMBER + " TEXT NOT NULL, " +
                COLUMN_GCASH_ORDER_NUMBER + " TEXT NOT NULL, " +
                COLUMN_GCASH_REFERENCE_NUMBER + " TEXT NOT NULL, " +
                COLUMN_GCASH_SENDER_NAME + " TEXT NOT NULL, " +
                COLUMN_GCASH_SENDER_NUMBER + " TEXT NOT NULL, " +
                COLUMN_GCASH_AMOUNT_RECEIVED + " REAL NOT NULL, " +
                COLUMN_GCASH_WHOLE_MESSAGE + " TEXT NOT NULL, " +
                COLUMN_GCASH_DATE_SENT + " TEXT NOT NULL, " +
                COLUMN_GCASH_COUNTER_RECEIVER + " TEXT NOT NULL)";

        db.execSQL(createPendingGCashOrdersTable);

        String createDTItable = "CREATE TABLE " + TABLE_DTI + " (" +
                COLUMN_DTI_NUMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LAST_DTI_NUMBER + " INTEGER)";
        db.execSQL(createDTItable);

        // Insert the initial order number value
        ContentValues DTIValues = new ContentValues();
        DTIValues.put(COLUMN_LAST_DTI_NUMBER, 1); // Set your desired initial order number
        db.insert(TABLE_DTI, null, DTIValues);

        String createQRTable = "CREATE TABLE " + TABLE_QR + " (" +
                COLUMN_QR_NUMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_QR_TEXT + " TEXT NOT NULL)";
        db.execSQL(createQRTable);

        // Insert the initial order number value
        ContentValues QRValues = new ContentValues();
        QRValues.put(COLUMN_QR_TEXT, "Place a website link"); // Set your desired initial order number
        db.insert(TABLE_QR, null, QRValues);

        String createORtable = "CREATE TABLE " + TABLE_OR_NUMBER + " (" +
                COLUMN_OR_NUMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_OR_NUMBER + " INTEGER)";
        db.execSQL(createORtable);

        // Insert the initial order number value
        ContentValues ORValues = new ContentValues();
        ORValues.put(COLUMN_OR_NUMBER, 1); // Set your desired initial order number
        db.insert(TABLE_OR_NUMBER, null, ORValues);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            // Step 1: Create temporary tables with the new schema
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTION + "_temp AS SELECT * FROM " + TABLE_TRANSACTION);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "_temp AS SELECT * FROM " + TABLE_USERS);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_INVENTORY + "_temp AS SELECT * FROM " + TABLE_INVENTORY);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MENU + "_temp AS SELECT * FROM " + TABLE_MENU);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS + "_temp AS SELECT * FROM " + TABLE_ORDERS);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PENDING_ORDERS + "_temp AS SELECT * FROM " + TABLE_PENDING_ORDERS);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ORDER_NUMBER + "_temp AS SELECT * FROM " + TABLE_ORDER_NUMBER);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RECEIPT + "_temp AS SELECT * FROM " + TABLE_RECEIPT);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RECEIPT_NUMBER + "_temp AS SELECT * FROM " + TABLE_RECEIPT_NUMBER);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_GCASH + "_temp AS SELECT * FROM " + TABLE_GCASH);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DTI + "_temp AS SELECT * FROM " + TABLE_DTI);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_QR + "_temp AS SELECT * FROM " + TABLE_QR);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_OR_NUMBER + "_temp AS SELECT * FROM " + TABLE_OR_NUMBER);

            // Step 2: Copy data to temporary tables
            copyDataToTemporaryTable(db, TABLE_TRANSACTION, TABLE_TRANSACTION + "_temp");
            copyDataToTemporaryTable(db, TABLE_USERS, TABLE_USERS + "_temp");
            copyDataToTemporaryTable(db, TABLE_INVENTORY, TABLE_INVENTORY + "_temp");
            copyDataToTemporaryTable(db, TABLE_MENU, TABLE_MENU + "_temp");
            copyDataToTemporaryTable(db, TABLE_ORDERS, TABLE_ORDERS + "_temp");
            copyDataToTemporaryTable(db, TABLE_PENDING_ORDERS, TABLE_PENDING_ORDERS + "_temp");
            copyDataToTemporaryTable(db, TABLE_ORDER_NUMBER, TABLE_ORDER_NUMBER + "_temp");
            copyDataToTemporaryTable(db, TABLE_RECEIPT, TABLE_RECEIPT + "_temp");
            copyDataToTemporaryTable(db, TABLE_RECEIPT_NUMBER, TABLE_RECEIPT_NUMBER + "_temp");
            copyDataToTemporaryTable(db, TABLE_GCASH, TABLE_GCASH + "_temp");
            copyDataToTemporaryTable(db, TABLE_DTI, TABLE_DTI + "_temp");
            copyDataToTemporaryTable(db, TABLE_QR, TABLE_QR + "_temp");
            copyDataToTemporaryTable(db, TABLE_OR_NUMBER, TABLE_OR_NUMBER + "_temp");

            // Step 3: Drop old tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PENDING_ORDERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_NUMBER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECEIPT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECEIPT_NUMBER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GCASH);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DTI);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QR);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_OR_NUMBER);

            // Step 4: Rename temporary tables to original table names
            db.execSQL("ALTER TABLE " + TABLE_TRANSACTION + "_temp RENAME TO " + TABLE_TRANSACTION);
            db.execSQL("ALTER TABLE " + TABLE_USERS + "_temp RENAME TO " + TABLE_USERS);
            db.execSQL("ALTER TABLE " + TABLE_INVENTORY + "_temp RENAME TO " + TABLE_INVENTORY);
            db.execSQL("ALTER TABLE " + TABLE_MENU + "_temp RENAME TO " + TABLE_MENU);
            db.execSQL("ALTER TABLE " + TABLE_ORDERS + "_temp RENAME TO " + TABLE_ORDERS);
            db.execSQL("ALTER TABLE " + TABLE_PENDING_ORDERS + "_temp RENAME TO " + TABLE_PENDING_ORDERS);
            db.execSQL("ALTER TABLE " + TABLE_ORDER_NUMBER + "_temp RENAME TO " + TABLE_ORDER_NUMBER);
            db.execSQL("ALTER TABLE " + TABLE_RECEIPT + "_temp RENAME TO " + TABLE_RECEIPT);
            db.execSQL("ALTER TABLE " + TABLE_RECEIPT_NUMBER + "_temp RENAME TO " + TABLE_RECEIPT_NUMBER);
            db.execSQL("ALTER TABLE " + TABLE_GCASH + "_temp RENAME TO " + TABLE_GCASH);
            db.execSQL("ALTER TABLE " + TABLE_DTI + "_temp RENAME TO " + TABLE_DTI);
            db.execSQL("ALTER TABLE " + TABLE_QR + "_temp RENAME TO " + TABLE_QR);
            db.execSQL("ALTER TABLE " + TABLE_OR_NUMBER + "_temp RENAME TO " + TABLE_OR_NUMBER);

            // Perform any additional steps or modifications as needed

    }

    private void copyDataToTemporaryTable(SQLiteDatabase db, String sourceTable, String destinationTable) {
        db.execSQL("INSERT INTO " + destinationTable + " SELECT * FROM " + sourceTable);
    }



}
