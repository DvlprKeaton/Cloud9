package com.example.pos.reports;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.print.PrintHelper;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pos.DataAccess;
import com.example.pos.DatabaseHelper;
import com.example.pos.DatePickerDialogFragment;
import com.example.pos.ExcelExporter;
import com.example.pos.Menu;
import com.example.pos.PrintTask;
import com.example.pos.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ReceiptReportsFragment extends Fragment {

    private DataAccess dataAccess;
    private String startDate, endDate;
    private List<String> receiptNumbers;
    private List<String> orderNumbers;
    private List<Double> amounts;
    private List<String> users;
    private List<String> dates;
    private Button startDate_btn, endDate_btn;
    private EditText startDate_ET, endDate_ET;
    private static final int PERMISSION_REQUEST_CODE = 1;
    String qrText,dti,bir;
    public ReceiptReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_receipt_reports, container, false);

        // Assuming you have a reference to the TableLayout in the fragment layout
        TableLayout tableLayout = view.findViewById(R.id.tableLayout);

        // Initialize the dataAccess object
        dataAccess = new DataAccess(getContext());

        // Retrieve the data from the dataAccess class
        Cursor cursor = dataAccess.getListReceipts(startDate, endDate);

        tableLayout.removeAllViews();

        // Add the table header
        addTableHeader(tableLayout);

        if (cursor != null && cursor.moveToFirst()) {
            receiptNumbers = new ArrayList<>();
            orderNumbers = new ArrayList<>();
            amounts = new ArrayList<>();
            users = new ArrayList<>();
            dates = new ArrayList<>();

            // Iterate through the cursor to retrieve the data
            do {
                String receiptNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_NUMBER));
                String orderNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_NUMBER));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_TOTAL));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_BY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT));

                // Add the data to the corresponding lists
                receiptNumbers.add(receiptNumber);
                orderNumbers.add(orderNumber);
                amounts.add(amount);
                users.add(userName);
                dates.add(date);
            } while (cursor.moveToNext());

            // Close the cursor after use
            cursor.close();

            for (int i = 0; i < receiptNumbers.size(); i++) {
                // Retrieve the values from the lists
                String receiptNumber = receiptNumbers.get(i);
                String orderNumber = orderNumbers.get(i);
                double amount = amounts.get(i);
                String user = users.get(i);
                String date = dates.get(i);

                // Create a new table row
                TableRow row = new TableRow(getContext());

                // Set the background color for alternating rows
                if (i % 2 == 0) {
                    row.setBackgroundColor(Color.WHITE); // Set the color for even rows
                } else {
                    row.setBackgroundColor(Color.LTGRAY); // Set the color for odd rows
                }

                // Convert dp to pixels
                float scale = getResources().getDisplayMetrics().density;
                int paddingInPixels = (int) (10 * scale + 0.5f); // 10dp converted to pixels

                TextView receiptTextView = createTextView(String.valueOf(receiptNumber), false);
                TextView orderTextView = createTextView(String.valueOf(orderNumber), false);
                TextView amountTextView = createTextView(String.valueOf(amount), false);
                TextView userTextView = createTextView(String.valueOf(user), false);
                TextView dateTextView = createTextView(date, false);

                row.addView(receiptTextView);
                row.addView(orderTextView);
                row.addView(amountTextView);
                row.addView(userTextView);
                row.addView(dateTextView);

                // Create a final copy of the orderNumber variable
                final String orderNumberCopy = orderNumber;

                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Use the final copy of orderNumber
                        showReceipt(Integer.parseInt(orderNumberCopy));
                        Toast.makeText(getContext(), "Receipt is Clicked - " +orderNumberCopy, Toast.LENGTH_SHORT).show();
                    }
                });

                // Add the table row to the table layout
                tableLayout.addView(row);
            }
        }

        Button exportExcel = view.findViewById(R.id.exportExcel_btn);
        exportExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a list to hold the data for exporting
                List<String[]> data = new ArrayList<>();

                // Iterate over the table rows to retrieve the data
                for (int i = 0; i < tableLayout.getChildCount(); i++) {
                    TableRow row = (TableRow) tableLayout.getChildAt(i);

                    // Retrieve the values from the TextViews in the table row
                    String receiptNumber = ((TextView) row.getChildAt(0)).getText().toString();
                    String orderNumber = ((TextView) row.getChildAt(1)).getText().toString();
                    String amount = ((TextView) row.getChildAt(2)).getText().toString();
                    String user = ((TextView) row.getChildAt(3)).getText().toString();
                    String date = ((TextView) row.getChildAt(4)).getText().toString();

                    // Create an array with the row data
                    String[] rowData = {receiptNumber, orderNumber, amount, user, date};

                    // Add the row data to the data list
                    data.add(rowData);
                }

                // Pass the data list and the current context to the exportToExcel method
                ExcelExporter.exportToExcel(requireContext(), null, data, "ReceiptReport", startDate, endDate);
            }
        });

        startDate_btn = view.findViewById(R.id.button_start_date);
        endDate_btn = view.findViewById(R.id.button_end_date);
        startDate_ET = view.findViewById(R.id.editText_start_date);
        endDate_ET = view.findViewById(R.id.editText_end_date);

        startDate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(true);
            }
        });

        endDate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(false);
            }
        });

        return view;
    }

    private TextView createTextView(String text, boolean isHeader) {
        float scale = getResources().getDisplayMetrics().density;
        int paddingInPixels = (int) (10 * scale + 0.5f); // 10dp converted to pixels

        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16); // Set the desired text size
        textView.setPadding(paddingInPixels, 5, paddingInPixels, 5); // Add padding (in pixels) to the TextView

        if (isHeader) {
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        return textView;
    }

    private void addTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(getContext());
        headerRow.setBackgroundColor(getResources().getColor(R.color.primary));

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        TextView receiptNumberHeaderTextView = createTextView("Receipt #", true);
        TextView orderNumberHeaderTextView = createTextView("Order #", true);
        TextView amountHeaderTextView = createTextView("Amount", true);
        TextView transactByHeaderTextView = createTextView("Transact By", true);
        TextView dateHeaderTextView = createTextView("Date", true);

        headerRow.addView(receiptNumberHeaderTextView);
        headerRow.addView(orderNumberHeaderTextView);
        headerRow.addView(amountHeaderTextView);
        headerRow.addView(transactByHeaderTextView);
        headerRow.addView(dateHeaderTextView);

        tableLayout.addView(headerRow);
    }



    private void fetchDataAndPopulateTable() {
        // Retrieve the data from the dataAccess class using the selected dates
        Cursor cursor = dataAccess.getListReceipts(startDate, endDate);

        // Clear the table layout before repopulating
        TableLayout tableLayout = getView().findViewById(R.id.tableLayout);
        tableLayout.removeAllViews();

        // Add the table header
        addTableHeader(tableLayout);

        if (cursor != null && cursor.moveToFirst()) {
            receiptNumbers = new ArrayList<>();
            orderNumbers = new ArrayList<>();
            amounts = new ArrayList<>();
            users = new ArrayList<>();
            dates = new ArrayList<>();

            // Iterate through the cursor to retrieve the data
            do {
                String receiptNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_NUMBER));
                String orderNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_NUMBER));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_TOTAL));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_BY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT));

                // Add the data to the corresponding lists
                receiptNumbers.add(receiptNumber);
                orderNumbers.add(orderNumber);
                amounts.add(amount);
                users.add(userName);
                dates.add(date);
            } while (cursor.moveToNext());

            // Close the cursor after use
            cursor.close();

            for (int i = 0; i < receiptNumbers.size(); i++) {
                // Retrieve the values from the lists
                String receiptNumber = receiptNumbers.get(i);
                String orderNumber = orderNumbers.get(i);
                double amount = amounts.get(i);
                String user = users.get(i);
                String date = dates.get(i);

                // Create a new table row
                TableRow row = new TableRow(getContext());

                // Set the background color for alternating rows
                if (i % 2 == 0) {
                    row.setBackgroundColor(Color.WHITE); // Set the color for even rows
                } else {
                    row.setBackgroundColor(Color.LTGRAY); // Set the color for odd rows
                }

                // Convert dp to pixels
                float scale = getResources().getDisplayMetrics().density;
                int paddingInPixels = (int) (10 * scale + 0.5f); // 10dp converted to pixels

                TextView receiptTextView = createTextView(String.valueOf(receiptNumber), false);
                TextView orderTextView = createTextView(String.valueOf(orderNumber), false);
                TextView amountTextView = createTextView(String.valueOf(amount), false);
                TextView userTextView = createTextView(String.valueOf(user), false);
                TextView dateTextView = createTextView(date, false);

                row.addView(receiptTextView);
                row.addView(orderTextView);
                row.addView(amountTextView);
                row.addView(userTextView);
                row.addView(dateTextView);

                // Create a final copy of the orderNumber variable
                final String orderNumberCopy = orderNumber;

                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Use the final copy of orderNumber
                        showReceipt(Integer.parseInt(orderNumberCopy));
                        Toast.makeText(getContext(), "Receipt is Clicked - " +orderNumberCopy, Toast.LENGTH_SHORT).show();
                    }
                });

                // Add the table row to the table layout
                tableLayout.addView(row);
            }
        }
    }

    @SuppressLint("Range")
    private void showReceipt(int orderNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.receipt_confimation_dialog, null);
        builder.setView(dialogView);

        // Find views in the custom layout
        TextView transactionNumberTextView = dialogView.findViewById(R.id.transactionNumber);
        TextView receiptNumberTextView = dialogView.findViewById(R.id.receiptNumber);
        TextView totalPriceTextView = dialogView.findViewById(R.id.totalPrice);
        TextView totalPaymentTextView = dialogView.findViewById(R.id.totalPayment);
        TextView totalChangeTextView = dialogView.findViewById(R.id.totalChange);
        TableLayout tableLayout = dialogView.findViewById(R.id.tableLayout);
        Button print = dialogView.findViewById(R.id.Print);

        ImageView qrCode = dialogView.findViewById(R.id.qrCode);
        TextView employeeName = dialogView.findViewById(R.id.employeeName);
        TextView transactionType = dialogView.findViewById(R.id.transactionType);
        View discountView = dialogView.findViewById(R.id.discountView);
        LinearLayout discountLayout = dialogView.findViewById(R.id.discountLayout);
        TextView discountType = dialogView.findViewById(R.id.discountType);
        TextView discountPrice = dialogView.findViewById(R.id.discountPrice);
        TextView paymentType = dialogView.findViewById(R.id.paymentType);
        TextView dtiNumber = dialogView.findViewById(R.id.dtiNumber);
        TextView datePrinted = dialogView.findViewById(R.id.datePrinted);
        TextView birNumber = dialogView.findViewById(R.id.birNumber);


        transactionNumberTextView.setText(""+orderNumber);

        tableLayout.removeAllViews();

        addTableHeaderReceipt(tableLayout);

        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, Double> itemPrices = dataAccess.getOrdersPrices(orderNumber); // Fetch the item prices

        Cursor cursor = dataAccess.getReceiptOnly(orderNumber);
        Cursor orderCursor = dataAccess.getOrders(orderNumber);

        while (orderCursor.moveToNext()) {
            String itemName = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_NAME));

            if (itemQuantities.containsKey(itemName)) {
                int currentQuantity = itemQuantities.get(itemName);
                itemQuantities.put(itemName, currentQuantity + 1);
            } else {
                itemQuantities.put(itemName, 1);
            }
        }

        cursor.moveToFirst();
        orderCursor.moveToFirst();

        String receiptId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ID));
        String receiptNumber = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_NUMBER));
        String orderQuantity = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_QUANTITY));
        String orderNote = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_NOTE));
        String orderDiscountID = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_DISCOUNT_TYPE));
        String orderType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_TYPE));
        String orderDiscount = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_DISCOUNT));
        String orderDiscountType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_DISCOUNT_TYPE));
        String orderPaymentType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_PAYMENT_TYPE));
        String orderPayment = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_PAYMENT));
        String orderTotal = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_TOTAL));
        String orderChange = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_CHANGE));
        String totalPaymentValue = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_PAYMENT));
        String addedBy = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_BY));
        String addedAt = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT));

        datePrinted.setText(addedAt);

        Cursor cursorDTI = dataAccess.getDTINumber();

        if (cursorDTI != null && cursorDTI.moveToFirst()) {
            do {
                // Retrieve the value from the cursor for the COLUMN_LAST_ORDER_NUMBER column
                @SuppressLint("Range") String lastDTI = cursorDTI.getString(cursorDTI.getColumnIndex(DatabaseHelper.COLUMN_LAST_DTI_NUMBER));

                // 2023030090866-07
                // Process the retrieved value as needed
                // For example, you can print it or assign it to a variable

                StringBuilder result = new StringBuilder();
                int count = 0;

                // Iterate through the input string from right to left
                for (int i = lastDTI.length() - 1; i >= 0; i--) {
                    char c = lastDTI.charAt(i);

                    // Check if the character is a digit (0-9)
                    if (Character.isDigit(c)) {
                        result.insert(0, c); // Add the digit to the result
                        count++; // Increase the count of digits found

                        // Break the loop when we have found five digits
                        if (count == 2) {
                            break;
                        }
                    }
                }

                String firstnumbers = lastDTI.replace(result,"");

                dti = firstnumbers + "-" + result;
                dtiNumber.setText(dti);
            } while (cursorDTI.moveToNext());
        }

        Cursor cursorQR = dataAccess.getQRCode();

        if (cursorQR != null && cursorQR.moveToFirst()) {
            do {
                // Retrieve the value from the cursor for the COLUMN_LAST_ORDER_NUMBER column
                @SuppressLint("Range") String lastQR = cursorQR.getString(cursorQR.getColumnIndex(DatabaseHelper.COLUMN_QR_TEXT));

                // Process the retrieved value as needed
                // For example, you can print it or assign it to a variable

                qrText = lastQR;
            } while (cursorQR.moveToNext());
        }

        Cursor cursorOR = dataAccess.getOR();

        if (cursorOR != null && cursorOR.moveToFirst()) {
            do {
                // Retrieve the value from the cursor for the COLUMN_LAST_ORDER_NUMBER column
                @SuppressLint("Range") String lastOR = cursorOR.getString(cursorOR.getColumnIndex(DatabaseHelper.COLUMN_OR_NUMBER));

                // Process the retrieved value as needed
                // For example, you can print it or assign it to a variable

                //birNum = String.valueOf(lastReceiptNumber);
                // Initialize variables to store the result
                StringBuilder result = new StringBuilder();
                int count = 0;

                // Iterate through the input string from right to left
                for (int i = lastOR.length() - 1; i >= 0; i--) {
                    char c = lastOR.charAt(i);

                    // Check if the character is a digit (0-9)
                    if (Character.isDigit(c)) {
                        result.insert(0, c); // Add the digit to the result
                        count++; // Increase the count of digits found

                        // Break the loop when we have found five digits
                        if (count == 5) {
                            break;
                        }
                    }
                }


                String firstnumbers = lastOR.replace(result,"");

                // Split the string into groups of three digits from the right (except the last group)
                List<String> groups = new ArrayList<>();
                int length = firstnumbers.length();
                for (int i = length; i > 0; i -= 3) {
                    int startIndex = Math.max(i - 3, 0);
                    groups.add(firstnumbers.substring(startIndex, i));
                }
                Collections.reverse(groups);

                // Join the groups with hyphens
                String resultFirst = TextUtils.join("-", groups);

                // Append '00000' at the end
                bir = resultFirst +"-"+ result;

                birNumber.setText(bir);
            } while (cursorOR.moveToNext());
        }


        discountLayout.setVisibility(View.GONE);
        discountView.setVisibility(View.GONE);
        discountPrice.setVisibility(View.GONE);
        if (!orderDiscountType.equals("No Discount")){
            discountLayout.setVisibility(View.VISIBLE);
            discountView.setVisibility(View.VISIBLE);
            discountPrice.setVisibility(View.VISIBLE);

            discountType.setText(orderDiscountType);
            discountPrice.setText(orderDiscount + "%");
        }

        String link = qrText;
        Bitmap qrCodeBitmap = generateQRCode(link, 500, 500);
        qrCode.setImageBitmap(qrCodeBitmap);

        employeeName.setText("Hi! My name is " + addedBy);
        String capitalizedOrderType = orderType.substring(0, 1).toUpperCase() + orderType.substring(1);
        transactionType.setText(capitalizedOrderType);

        paymentType.setText(orderPaymentType);

        receiptNumberTextView.setText(receiptNumber);
        totalPriceTextView.setText("₱"+ orderTotal);
        totalPaymentTextView.setText("₱"+ orderPayment);
        totalChangeTextView.setText("₱"+ orderChange);

        // Create a Map to store item notes
        Map<String, String> itemNotes = new HashMap<>();

        // Loop through the orderCursor to retrieve item names and notes
        while (orderCursor.moveToNext()) {
            String itemName = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_NAME));
            String itemNote = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_NOTE));
            String itemDiscountID = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_DISCOUNT_TYPE));


            // Add the item note to the itemNotes map using the item name as the key
            itemNotes.put(itemName, itemNote);
        }

        // Now, use the itemNotes map to display the items and their corresponding notes
        int totalItems = itemQuantities.size(); // Get the total number of items
        int currentItem = 0; // Initialize the current item counter
        int itemNumber = 1;

        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            TableRow tableRow = new TableRow(getContext());

            TextView itemNameTextView = createTextViewTab(entry.getKey(), false);
            TextView quantityTextView = createTextViewTab(String.valueOf(orderQuantity), false);

            // Get the price for the current item
            double itemPrice = itemPrices.get(entry.getKey());
            Log.d("itemPriceMenu", String.valueOf(itemPrice) + orderQuantity);

            TextView priceTextView = createTextViewTab(String.valueOf(itemPrice), false);

            TableRow.LayoutParams cellLayoutParams = new TableRow.LayoutParams(
                    0,                                      // Set width to 0 to use weight
                    TableRow.LayoutParams.WRAP_CONTENT,     // Set height to WRAP_CONTENT
                    1f                                      // Set weight to 1 to take up available space
            );
            itemNameTextView.setLayoutParams(cellLayoutParams);
            quantityTextView.setLayoutParams(cellLayoutParams);
            priceTextView.setLayoutParams(cellLayoutParams);

            tableRow.addView(itemNameTextView);
            tableRow.addView(quantityTextView);
            tableRow.addView(priceTextView);

            // Adjust layout parameters to match header row weights
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            tableLayout.addView(tableRow);

            // Retrieve the note for the current item from the itemNotes map
            String itemNote = itemNotes.get(entry.getKey());

            // Add note row below the current row if there's a note for the item
            if (itemNote != null && !itemNote.isEmpty() ) {
                TableRow noteTableRow = new TableRow(getContext());
                TextView noteTextView = createTextViewTab("\t\t\t- " + itemNote, true);

                // Set text alignment to the left
                noteTextView.setGravity(Gravity.LEFT);

                // Set text size (change the value as needed)
                noteTextView.setTextSize(16); // Change the text size to 16sp (adjust as needed)

                TableRow.LayoutParams noteLayoutParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                noteLayoutParams.leftMargin = 0; // Remove the left margin
                noteTextView.setLayoutParams(noteLayoutParams);
                noteTableRow.addView(noteTextView);
                tableLayout.addView(noteTableRow);
            }

// Add the order discount type as a new row above the items
            if (orderDiscountType != null && !orderDiscountType.isEmpty() && !orderDiscountType.equals("No Discount")) {
                TableRow discountTypeTableRow = new TableRow(getContext());
                TextView discountTypeTextView = createTextViewTab("Discount Type: " + orderDiscountType, true);

                // Set text alignment to the left
                discountTypeTextView.setGravity(Gravity.LEFT);

                // Set text size (change the value as needed)
                discountTypeTextView.setTextSize(16); // Change the text size to 16sp (adjust as needed)

                TableRow.LayoutParams discountTypeLayoutParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                discountTypeLayoutParams.leftMargin = 0; // Remove the left margin
                discountTypeTextView.setLayoutParams(discountTypeLayoutParams);
                discountTypeTableRow.addView(discountTypeTextView);
                tableLayout.addView(discountTypeTableRow);
            }

            currentItem++;

            // Check if it's the last item, and add the note row if necessary
            if (currentItem == totalItems && orderNote != null && !orderNote.isEmpty()) {
                TableRow lastItemNoteRow = new TableRow(getContext());
                TextView lastItemNoteTextView = createTextViewTab("\t\t\t- " + orderNote, true);

                // Set text alignment to the left
                lastItemNoteTextView.setGravity(Gravity.LEFT);

                // Set text size (change the value as needed)
                lastItemNoteTextView.setTextSize(16); // Change the text size to 16sp (adjust as needed)

                TableRow.LayoutParams lastItemNoteLayoutParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                lastItemNoteLayoutParams.leftMargin = 0; // Remove the left margin
                lastItemNoteTextView.setLayoutParams(lastItemNoteLayoutParams);
                lastItemNoteRow.addView(lastItemNoteTextView);
                tableLayout.addView(lastItemNoteRow);
            }

            itemNumber++;
            cursor.moveToNext();
        }


        cursor.close();


        AlertDialog dialog = builder.create();
        dialog.show();

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Storage permission not granted, request it
                    requestStoragePermission();
                } else {
                    // Storage permission granted, proceed with capturing and saving the image
                    print.setVisibility(View.GONE); // Hide the print button

                    LinearLayout linearLayout = (LinearLayout) dialogView;

// Create a bitmap of the linearLayout
                    linearLayout.setDrawingCacheEnabled(true);
                    linearLayout.measure(View.MeasureSpec.makeMeasureSpec(linearLayout.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    linearLayout.layout(0, 0, linearLayout.getMeasuredWidth(), linearLayout.getMeasuredHeight());

                    Bitmap bitmap = Bitmap.createBitmap(linearLayout.getWidth(), linearLayout.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    linearLayout.draw(canvas);

                    linearLayout.setDrawingCacheEnabled(false);

// Now you have the bitmap of the entire content of the LinearLayout


                    print.setVisibility(View.VISIBLE); // Show the print button again

                    int receipt_no = dataAccess.receiptNumber();
                    int order_no = dataAccess.orderNumber();

                    LocalDateTime currentDateTime = LocalDateTime.now();
                    String date_in = currentDateTime.toString();

                    String image_name = receipt_no + "-" + order_no + "-" + date_in;

                    String relativePath = Environment.DIRECTORY_DCIM + File.separator + "Receipt";

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, image_name + ".png");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);

                    Uri imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    OutputStream outputStream = null;

                    try {
                        outputStream = getActivity().getContentResolver().openOutputStream(imageUri);
                        if (outputStream != null) {
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Session", Context.MODE_PRIVATE);

                            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                            String username = sharedPreferences.getString("username", "");
                            String userRole = sharedPreferences.getString("userRole", "");

                            // Get the current date and time
                            String updatedAt = currentDateTime.toString();
                            dataAccess.insertMovement(0, username, "Printed a receipt with a receipt # of " + receipt_no + " order # of " + order_no, updatedAt);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            outputStream.close();

                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                            BluetoothSocket socket = null;
                            OutputStream outputStreamPrint = null;

                            for (BluetoothDevice device : pairedDevices) {
                                if (device.getName().equals("Printer001")) {
                                    // Connect to the selected printer
                                    // You can use a BluetoothSocket to establish a connection
                                    UUID uuid = device.getUuids()[0].getUuid();
                                    try {
                                        socket = device.createRfcommSocketToServiceRecord(uuid);
                                        socket.connect();
                                        outputStreamPrint = socket.getOutputStream();
                                        break;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        // Handle connection error
                                    }
                                }
                            }

                            // Connect to the selected printer and establish the Bluetooth socket
                            if (socket != null && outputStreamPrint != null) {
                                View rootViewToPrint = dialogView.getRootView();
                                PrintTask printTask = new PrintTask( getContext(), socket, outputStreamPrint, bitmap);
                                printTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }



                            Toast.makeText(getContext(), "Receipt saved as image", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to save receipt image", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    // Helper method to convert a View to a Bitmap
    private Bitmap rootViewToBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private Bitmap generateQRCode(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            int bitMatrixWidth = bitMatrix.getWidth();
            int bitMatrixHeight = bitMatrix.getHeight();
            Bitmap qrCodeBitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.RGB_565);

            for (int x = 0; x < bitMatrixWidth; x++) {
                for (int y = 0; y < bitMatrixHeight; y++) {
                    qrCodeBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return qrCodeBitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private TextView createTextViewTab(String text, boolean isHeader) {
        float scale = getResources().getDisplayMetrics().density;
        int paddingInPixels = (int) (10 * scale + 0.5f); // 10dp converted to pixels

        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(25); // Set the desired text size
        textView.setPadding(paddingInPixels, 5, paddingInPixels, 5); // Add padding (in pixels) to the TextView
        textView.setEllipsize(null); // Disable ellipsize
        textView.setMaxLines(Integer.MAX_VALUE); // Set maxLines to a high value for wrapping

        // Set the layout_width and layout_height attributes
        textView.setLayoutParams(new TableRow.LayoutParams(300, TableRow.LayoutParams.WRAP_CONTENT));

        if (isHeader) {
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        return textView;
    }


    private void showDatePickerDialog(boolean isStartDate) {
        DatePickerDialogFragment datePickerDialogFragment = DatePickerDialogFragment.newInstance();
        datePickerDialogFragment.setOnDateSelectedListener(new DatePickerDialogFragment.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day) {
                String formattedDate = formatDate(year, month, day);

                if (isStartDate) {
                    startDate_ET.setText(formattedDate);
                    startDate = formattedDate;
                } else {
                    endDate_ET.setText(formattedDate);
                    endDate = formattedDate;
                }
                dataAccess.getListReceipts(startDate, endDate);
                fetchDataAndPopulateTable();
            }
        });
        datePickerDialogFragment.show(getParentFragmentManager(), "datePicker");
    }

    private String formatDate(int year, int month, int day) {
        // Format the date as per your requirements
        // This is just a simple example, you can adjust it to match your desired format
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Explain why the permission is needed
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Permission Request");
            builder.setMessage("This app requires storage permission to save the receipt image. Please grant the permission to continue.");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Request the permission
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                }
            });

            builder.show();
        } else {
            // Request the permission without explaining
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with saving the PDF
                // ...
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(getContext(), "Storage permission required to save the PDF file", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(10, 20, 10, 20);
        textView.setTextSize(25);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT
        ));
        return textView;
    }

    private void addTableHeaderReceipt(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(getContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        headerRow.setGravity(Gravity.CENTER); // Set the gravity to center

        TextView receiptNumberHeaderTextView = createHeaderTextView("Item(s)");
        receiptNumberHeaderTextView.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                1f
        ));
        headerRow.addView(receiptNumberHeaderTextView);

        TextView orderNumberHeaderTextView = createHeaderTextView("Quantity");
        orderNumberHeaderTextView.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                1f
        ));
        headerRow.addView(orderNumberHeaderTextView);

        TextView amountHeaderTextView = createHeaderTextView("Price");
        amountHeaderTextView.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                1f
        ));
        headerRow.addView(amountHeaderTextView);

        tableLayout.addView(headerRow);
    }



}