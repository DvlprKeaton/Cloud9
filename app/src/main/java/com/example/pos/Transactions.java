package com.example.pos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Transactions extends AppCompatActivity {
    private DataAccess dataAccess;
    private String endDate;
    private List<String> transactionIDs;
    private List<String> usernames;
    private List<String> remarks;
    private List<String> dates;
    private Button endDate_btn;
    private EditText endDate_ET;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        // Assuming you have a reference to the TableLayout in the fragment layout
        TableLayout tableLayout = findViewById(R.id.tableLayout);

        // Initialize the dataAccess object
        dataAccess = new DataAccess(this);
        dataAccess.getAllItems(this);

        SharedPreferences sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", "");
        String userRole = sharedPreferences.getString("userRole", "");

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();
        dataAccess.insertMovement(0, username, "Redirected to the Transactions", updatedAt);


        NavigationView navigationView = findViewById(R.id.leftNavigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation item selection here
                int itemId = menuItem.getItemId();
                switch (itemId) {
                    case R.id.inventory:
                        // Start the new activity
                        Intent intentInventory = new Intent(Transactions.this, Inventory.class);
                        startActivity(intentInventory);
                        Transactions.this.overridePendingTransition(0, 0); // Remove transition animation
                        Transactions.this.finish();

                        break;
                    case R.id.staff:
                        // Start the new activity
                        Intent intentStaff = new Intent(Transactions.this, Staff.class);
                        startActivity(intentStaff);
                        Transactions.this.overridePendingTransition(0, 0); // Remove transition animation
                        Transactions.this.finish();

                        break;
                    case R.id.reports:
                        // Start the new activity
                        Intent intentReports = new Intent(Transactions.this, Reports.class);
                        startActivity(intentReports);
                        Transactions.this.overridePendingTransition(0, 0); // Remove transition animation
                        Transactions.this.finish();

                        break;
                    case R.id.cash_drawer:
                        // Start the new activity
                        Intent intentCash = new Intent(Transactions.this, Menu.class);
                        startActivity(intentCash);
                        Transactions.this.overridePendingTransition(0, 0); // Remove transition animation
                        Transactions.this.finish();

                        break;
                    case R.id.settings:
                        // Start the new activity
                        Intent intentSettings = new Intent(Transactions.this, Settings.class);
                        startActivity(intentSettings);
                        Transactions.this.overridePendingTransition(0, 0); // Remove transition animation
                        Transactions.this.finish();

                        break;
                    case R.id.logout:
                        DataAccess dataAccess = new DataAccess(Transactions.this); // Initialize the DataAccess object
                        dataAccess.logoutUser(getApplicationContext()); // Call the logoutUser method

                        // Finish the current activity
                        finish();
                        break;
                    default:
                        // Handle other menu items
                        break;
                }

                // Return true to indicate that the item selection has been handled
                return true;
            }
        });


        // Retrieve the data from the dataAccess class
        Cursor cursor = dataAccess.getTransactions(endDate);

        tableLayout.removeAllViews();

        // Add the table header
        addTableHeader(tableLayout);


        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );

        if (cursor != null && cursor.moveToFirst()) {
            transactionIDs = new ArrayList<>();
            usernames = new ArrayList<>();
            remarks = new ArrayList<>();
            dates = new ArrayList<>();

            // Iterate through the cursor to retrieve the data
            do {
                String trasactionID = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_ID));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_USER_NAME));
                String remark = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_MOVEMENT));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_ADDED_AT));

                // Add the data to the corresponding lists
                transactionIDs.add(trasactionID);
                usernames.add(userName);
                remarks.add(remark);
                dates.add(date);
            } while (cursor.moveToNext());

            // Close the cursor after use
            cursor.close();

            for (int i = 0; i < transactionIDs.size(); i++) {
                // Retrieve the values from the lists
                String transactionID = transactionIDs.get(i);
                String name = usernames.get(i);
                String remark = remarks.get(i);
                String date = dates.get(i);

                // Create a new table row
                TableRow row = new TableRow(this);

                // Set the background color for alternating rows
                if (i % 2 == 0) {
                    row.setBackgroundColor(Color.WHITE); // Set the color for even rows
                } else {
                    row.setBackgroundColor(Color.LTGRAY); // Set the color for odd rows
                }

                TextView transactionIDTextView = createTextView(String.valueOf(transactionID), true);
                TextView nameTextView = createTextView(String.valueOf(name), true);
                TextView remarkTextView = createTextView(String.valueOf(remark), true);
                TextView dateTextView = createTextView(date, true);

                row.addView(transactionIDTextView);
                row.addView(nameTextView);
                row.addView(remarkTextView);
                row.addView(dateTextView);

                // Add the table row to the table layout
                tableLayout.addView(row, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                ));
            }
        }


        Button exportExcel = findViewById(R.id.exportExcel_btn);
        exportExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a list to hold the data for exporting
                List<String[]> data = new ArrayList<>();

                // Iterate over the table rows to retrieve the data
                for (int i = 0; i < tableLayout.getChildCount(); i++) {
                    TableRow row = (TableRow) tableLayout.getChildAt(i);

                    // Retrieve the values from the TextViews in the table row
                    String transactionID = ((TextView) row.getChildAt(0)).getText().toString();
                    String username = ((TextView) row.getChildAt(1)).getText().toString();
                    String remarks = ((TextView) row.getChildAt(2)).getText().toString();
                    String date = ((TextView) row.getChildAt(3)).getText().toString();

                    // Create an array with the row data
                    String[] rowData = {transactionID, username, remarks, date};

                    // Add the row data to the data list
                    data.add(rowData);
                }

                // Pass the data list and the current context to the exportToExcel method
                ExcelExporter.exportToExcel(Transactions.this,null, data, "TransactionReport", endDate, endDate);
            }
        });

        endDate_btn = findViewById(R.id.button_end_date);
        endDate_ET = findViewById(R.id.editText_end_date);


        endDate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(false);
            }
        });
    }



    private TextView createTextView(String text, boolean isHeader) {
        float scale = getResources().getDisplayMetrics().density;
        int paddingInPixels = (int) (10 * scale + 0.5f); // 10dp converted to pixels

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16); // Set the desired text size
        textView.setPadding(paddingInPixels, 5, paddingInPixels, 5); // Add padding (in pixels) to the TextView
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setMaxLines(1);

        // Set the layout_width attribute to wrap_content
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));

        if (isHeader) {
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        return textView;
    }


    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(10, 20, 10, 20);
        textView.setTextSize(16);
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }

    private void addTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(getResources().getColor(R.color.primary));

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );

        TextView receiptNumberHeaderTextView = createHeaderTextView("Transaction ID");
        receiptNumberHeaderTextView.setLayoutParams(params);
        headerRow.addView(receiptNumberHeaderTextView);

        TextView orderNumberHeaderTextView = createHeaderTextView("Username");
        orderNumberHeaderTextView.setLayoutParams(params);
        headerRow.addView(orderNumberHeaderTextView);

        TextView amountHeaderTextView = createHeaderTextView("Transaction");
        amountHeaderTextView.setLayoutParams(params);
        headerRow.addView(amountHeaderTextView);

        TextView transactByHeaderTextView = createHeaderTextView("Date");
        transactByHeaderTextView.setLayoutParams(params);
        headerRow.addView(transactByHeaderTextView);

        tableLayout.addView(headerRow);
    }


    private void fetchDataAndPopulateTable() {
        // Clear the table layout before repopulating
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        tableLayout.removeAllViews();

        // Retrieve the data from the dataAccess class
        Cursor cursor = dataAccess.getTransactions(endDate);

        // Add the table header
        addTableHeader(tableLayout);


        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );

        if (cursor != null && cursor.moveToFirst()) {
            transactionIDs = new ArrayList<>();
            usernames = new ArrayList<>();
            remarks = new ArrayList<>();
            dates = new ArrayList<>();

            // Iterate through the cursor to retrieve the data
            do {
                String trasactionID = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_ID));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_USER_NAME));
                String remark = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_MOVEMENT));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_ADDED_AT));

                // Add the data to the corresponding lists
                transactionIDs.add(trasactionID);
                usernames.add(userName);
                remarks.add(remark);
                dates.add(date);
            } while (cursor.moveToNext());

            // Close the cursor after use
            cursor.close();

            for (int i = 0; i < transactionIDs.size(); i++) {
                // Retrieve the values from the lists
                String transactionID = transactionIDs.get(i);
                String name = usernames.get(i);
                String remark = remarks.get(i);
                String date = dates.get(i);

                // Create a new table row
                TableRow row = new TableRow(this);

                // Set the background color for alternating rows
                if (i % 2 == 0) {
                    row.setBackgroundColor(Color.WHITE); // Set the color for even rows
                } else {
                    row.setBackgroundColor(Color.LTGRAY); // Set the color for odd rows
                }

                TextView transactionIDTextView = createTextView(String.valueOf(transactionID), true);
                TextView nameTextView = createTextView(String.valueOf(name), true);
                TextView remarkTextView = createTextView(String.valueOf(remark), true);
                TextView dateTextView = createTextView(date, true);

                row.addView(transactionIDTextView);
                row.addView(nameTextView);
                row.addView(remarkTextView);
                row.addView(dateTextView);

                // Add the table row to the table layout
                tableLayout.addView(row, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                ));
            }
        }
    }

    private void showDatePickerDialog(boolean isStartDate) {
        DatePickerDialogFragment datePickerDialogFragment = DatePickerDialogFragment.newInstance();
        datePickerDialogFragment.setOnDateSelectedListener(new DatePickerDialogFragment.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day) {
                String formattedDate = formatDate(year, month, day);

                endDate_ET.setText(formattedDate);
                endDate = formattedDate;

                dataAccess.getTransactions(endDate);
                fetchDataAndPopulateTable();
            }
        });
        datePickerDialogFragment.show(Transactions.this.getSupportFragmentManager(), "datePicker");
    }

    private String formatDate(int year, int month, int day) {
        // Format the date as per your requirements
        // This is just a simple example, you can adjust it to match your desired format
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
    }
}