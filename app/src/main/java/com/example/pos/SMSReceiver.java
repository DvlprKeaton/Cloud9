package com.example.pos;

import static com.example.pos.DatabaseHelper.COLUMN_LAST_ORDER_NUMBER;
import static com.example.pos.DatabaseHelper.COLUMN_LAST_RECEIPT_NUMBER;
import static com.example.pos.DatabaseHelper.TABLE_ORDER_NUMBER;
import static com.example.pos.DatabaseHelper.TABLE_RECEIPT_NUMBER;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.time.LocalDateTime;

public class SMSReceiver extends BroadcastReceiver {
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String SMS_SENDER = "GCash"; // Replace with the desired sender's phone number

    private DataAccess dataAccess;
    private DatabaseHelper dbHelper;

    @Override
    public void onReceive(Context context, Intent intent) {

        // Initialize the dataAccess object with the Application context
        dataAccess = new DataAccess(context);
        dbHelper = new DatabaseHelper(context);

        SharedPreferences sharedPreferences = context.getSharedPreferences("Session", Context.MODE_PRIVATE);


        String username = sharedPreferences.getString("username", "");

        LocalDateTime currentDateTime = LocalDateTime.now();
        String date_in = currentDateTime.toString();

        if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Object[] pdus = (Object[]) extras.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = sms.getOriginatingAddress();
                        String message = sms.getMessageBody();
                        if (sender.equals(SMS_SENDER) && message.startsWith("You have received")) {
                            String receivedDate = sms.getTimestampMillis() + "";
                            // Find the starting position of "from "
                            int startIndex = message.indexOf("from ") + 5;
                            // Find the ending position of " w/"
                            int endIndex = message.indexOf(" w/");
                            // Extract the sender information
                            String senderInfo = message.substring(startIndex, endIndex);
                            // Split the sender information into name and number
                            String[] parts = senderInfo.split("\\.");
                            // Remove spaces and asterisks from the name
                            String senderName = parts[0].trim();
                            // Remove spaces from the number
                            String senderNumber = parts[1].trim();

                            int refNoIndex = message.indexOf("Ref. No.");
                            String referenceNumber = message.substring(refNoIndex + 8).trim();

                            int amountIndex = message.indexOf("PHP ");
                            int amountEndIndex = message.indexOf(" of GCash");
                            String receivedAmount = message.substring(amountIndex + 4, amountEndIndex).trim();
                            double fin_receivedAmount = Double.parseDouble(receivedAmount);
                            dataAccess.insertGCash(referenceNumber, senderName, senderNumber,fin_receivedAmount,date_in,message, String.valueOf(receiptNumber()), String.valueOf(orderNumber()),username,context);


                            Toast.makeText(context, "" + receivedDate + " The Amount is: " + receivedAmount + " From " + senderName + " " + senderNumber + " with the reference no. " + referenceNumber, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
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
}
