package com.example.pos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
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

public class CashDrawer extends AppCompatActivity {

    private DataAccess dataAccess;
    private String endDate;
    private List<String> receiptNumbers;
    private List<String> orderNumbers;
    private List<Double> amounts;
    private List<String> users;
    private List<String> dates;
    private Button endDate_btn;
    private EditText endDate_ET;
    TextView totalValue;
    double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_drawer);

        // Assuming you have a reference to the TableLayout in the fragment layout
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        totalValue = findViewById(R.id.totalValue);

        // Initialize the dataAccess object
        dataAccess = new DataAccess(this);
        dataAccess.getAllItems(CashDrawer.this);

        SharedPreferences sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", "");
        String userRole = sharedPreferences.getString("userRole", "");

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();
        dataAccess.insertMovement(0, username, "Redirected to the Cash Drawer", updatedAt);


        NavigationView adminNavigationView = findViewById(R.id.leftNavigationView);
        NavigationView staffNavigationView = findViewById(R.id.staffleftNavigationView);

        if (userRole.equals("Admin")){
            adminNavigationView.setVisibility(View.VISIBLE);
            staffNavigationView.setVisibility(View.GONE);

        }else {
            adminNavigationView.setVisibility(View.GONE);
            staffNavigationView.setVisibility(View.VISIBLE);
        }

        adminNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation item selection here
                int itemId = menuItem.getItemId();
                switch (itemId) {
                    case R.id.menu:
                        // Start the new activity
                        Intent intentMenu = new Intent(CashDrawer.this, Menu.class);
                        startActivity(intentMenu);
                        CashDrawer.this.overridePendingTransition(0, 0); // Remove transition animation
                        CashDrawer.this.finish();

                    break;
                    case R.id.inventory:
                        // Start the new activity
                        Intent intentInventory = new Intent(CashDrawer.this, Inventory.class);
                        startActivity(intentInventory);
                        CashDrawer.this.overridePendingTransition(0, 0); // Remove transition animation
                        CashDrawer.this.finish();

                        break;
                    case R.id.staff:
                        // Start the new activity
                        Intent intentStaff = new Intent(CashDrawer.this, Staff.class);
                        startActivity(intentStaff);
                        CashDrawer.this.overridePendingTransition(0, 0); // Remove transition animation
                        CashDrawer.this.finish();

                        break;
                    case R.id.reports:
                        // Start the new activity
                        Intent intentReports = new Intent(CashDrawer.this, Reports.class);
                        startActivity(intentReports);
                        CashDrawer.this.overridePendingTransition(0, 0); // Remove transition animation
                        CashDrawer.this.finish();

                        break;
                    case R.id.transaction:
                        // Start the new activity
                        Intent intentTransactions = new Intent(CashDrawer.this, Transactions.class);
                        startActivity(intentTransactions);
                        CashDrawer.this.overridePendingTransition(0, 0); // Remove transition animation
                        CashDrawer.this.finish();

                        break;
                    case R.id.settings:
                        // Start the new activity
                        Intent intentSettings = new Intent(CashDrawer.this, Settings.class);
                        startActivity(intentSettings);
                        CashDrawer.this.overridePendingTransition(0, 0); // Remove transition animation
                        CashDrawer.this.finish();

                        break;
                    case R.id.logout:
                        DataAccess dataAccess = new DataAccess(CashDrawer.this); // Initialize the DataAccess object
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

        staffNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation item selection here
                int itemId = menuItem.getItemId();
                switch (itemId) {
                    case R.id.menu:
                        // Start the new activity
                        Intent intentMenu = new Intent(CashDrawer.this, Menu.class);
                        startActivity(intentMenu);
                        CashDrawer.this.overridePendingTransition(0, 0); // Remove transition animation
                        CashDrawer.this.finish();

                        break;
                    case R.id.logout:
                        DataAccess dataAccess = new DataAccess(CashDrawer.this); // Initialize the DataAccess object
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
        Cursor cursor = dataAccess.getCashDrawer(endDate);

        tableLayout.removeAllViews();

// Add the table header
        addTableHeader(tableLayout);

        totalAmount = 0.0;

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
                // Calculate the total amount
                totalAmount += amount;
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
                TableRow row = new TableRow(this);

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

                // Add the table row to the table layout
                tableLayout.addView(row);
            }
        }

        totalValue.setText("₱ " + totalAmount);

        Button exportExcel = findViewById(R.id.exportExcel_btn);
        if (userRole.equals("Admin")){
            exportExcel.setVisibility(View.VISIBLE);
        }else {
            exportExcel.setVisibility(View.GONE);
        }
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
                //ExcelExporter.exportToExcel(CashDrawer.this,null, data, "ReceiptReport", startDate, endDate);
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

    @SuppressLint("ResourceAsColor")
    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(10, 20, 10, 20);
        textView.setTextSize(16);
        textView.setTypeface(null, Typeface.BOLD);
        // Assuming you have a TextView instance named textView
        int colorResId = R.color.primary; // Replace `your_color_name` with the actual color name from colors.xml
        int primary = ContextCompat.getColor(this, colorResId);
        textView.setBackgroundColor(primary);
        return textView;
    }


    private void addTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(this);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );

        TextView receiptNumberHeaderTextView = createHeaderTextView("Receipt #");
        receiptNumberHeaderTextView.setLayoutParams(params);
        headerRow.addView(receiptNumberHeaderTextView);

        TextView orderNumberHeaderTextView = createHeaderTextView("Order #");
        orderNumberHeaderTextView.setLayoutParams(params);
        headerRow.addView(orderNumberHeaderTextView);

        TextView amountHeaderTextView = createHeaderTextView("Amount");
        amountHeaderTextView.setLayoutParams(params);
        headerRow.addView(amountHeaderTextView);

        TextView transactByHeaderTextView = createHeaderTextView("Transact By");
        transactByHeaderTextView.setLayoutParams(params);
        headerRow.addView(transactByHeaderTextView);

        TextView DateHeaderTextView = createHeaderTextView("Date");
        DateHeaderTextView.setLayoutParams(params);
        headerRow.addView(DateHeaderTextView);

        tableLayout.addView(headerRow);
    }

    private void fetchDataAndPopulateTable() {
        // Clear the table layout before repopulating
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        // Retrieve the data from the dataAccess class
        Cursor cursor = dataAccess.getCashDrawer(endDate);

        tableLayout.removeAllViews();

// Add the table header
        addTableHeader(tableLayout);

        totalAmount = 0.0;

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
                // Calculate the total amount
                totalAmount += amount;
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
                TableRow row = new TableRow(this);

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

                // Add the table row to the table layout
                tableLayout.addView(row);
            }
        }

        totalValue.setText("₱ " + totalAmount);
    }

    private void showDatePickerDialog(boolean isStartDate) {
        DatePickerDialogFragment datePickerDialogFragment = DatePickerDialogFragment.newInstance();
        datePickerDialogFragment.setOnDateSelectedListener(new DatePickerDialogFragment.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day) {
                String formattedDate = formatDate(year, month, day);

                    endDate_ET.setText(formattedDate);
                    endDate = formattedDate;

                dataAccess.getCashDrawer(endDate);
                fetchDataAndPopulateTable();
            }
        });
        datePickerDialogFragment.show(CashDrawer.this.getSupportFragmentManager(), "datePicker");
    }

    private String formatDate(int year, int month, int day) {
        // Format the date as per your requirements
        // This is just a simple example, you can adjust it to match your desired format
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
    }
}