package com.example.pos.reports;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.pos.DataAccess;
import com.example.pos.DatabaseHelper;
import com.example.pos.DatePickerDialogFragment;
import com.example.pos.ExcelExporter;
import com.example.pos.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SalesReportsFragment extends Fragment {

    private DataAccess dataAccess;
    private String startDate, endDate;
    private List<String> receiptNumbers;
    private List<String> orderNumbers;
    private List<Double> amounts;
    private List<String> dates;
    private Button startDate_btn, endDate_btn;
    private EditText startDate_ET, endDate_ET;

    public SalesReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sales_reports, container, false);

        // Assuming you have a reference to the TableLayout in the fragment layout
        TableLayout tableLayout = view.findViewById(R.id.tableLayout);

        // Initialize the dataAccess object
        dataAccess = new DataAccess(getContext());

        // Retrieve the data from the dataAccess class
        Cursor cursor = dataAccess.getTotalSales(startDate, endDate);

        tableLayout.removeAllViews();

        // Add the table header
        addTableHeader(tableLayout);

        if (cursor != null && cursor.moveToFirst()) {
            receiptNumbers = new ArrayList<>();
            orderNumbers = new ArrayList<>();
            amounts = new ArrayList<>();
            dates = new ArrayList<>();

            // Iterate through the cursor to retrieve the data
            do {
                String receiptNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_NUMBER));
                String orderNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_NUMBER));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_TOTAL));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT));

                // Add the data to the corresponding lists
                receiptNumbers.add(receiptNumber);
                orderNumbers.add(orderNumber);
                amounts.add(amount);
                dates.add(date);
            } while (cursor.moveToNext());

            // Close the cursor after use
            cursor.close();

            // Iterate through the data lists to populate the table rows
            for (int i = 0; i < receiptNumbers.size(); i++) {
                // Retrieve the values from the lists
                String receiptNumber = receiptNumbers.get(i);
                String orderNumber = orderNumbers.get(i);
                double amount = amounts.get(i);
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

                TextView amountTextView = createTextView(String.valueOf(amount), false);
                TextView dateTextView = createTextView(date, false);

                row.addView(amountTextView);
                row.addView(dateTextView);

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
                    String amount = ((TextView) row.getChildAt(0)).getText().toString();
                    String date = ((TextView) row.getChildAt(1)).getText().toString();

                    // Create an array with the row data
                    String[] rowData = {amount, date};

                    // Add the row data to the data list
                    data.add(rowData);
                }

                // Pass the data list and the current context to the exportToExcel method
                ExcelExporter.exportToExcel(requireContext(), null, data, "SalesReport", startDate, endDate);
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

        TextView amountHeaderTextView = createTextView("Amount", true);
        TextView dateHeaderTextView = createTextView("Date", true);

        headerRow.addView(amountHeaderTextView);
        headerRow.addView(dateHeaderTextView);

        tableLayout.addView(headerRow);
    }
    private void fetchDataAndPopulateTable() {
        // Retrieve the data from the dataAccess class using the selected dates
        Cursor cursor = dataAccess.getTotalSales(startDate, endDate);

        // Clear the table layout before repopulating
        TableLayout tableLayout = getView().findViewById(R.id.tableLayout);
        tableLayout.removeAllViews();

        // Add the table header
        addTableHeader(tableLayout);

        if (cursor != null && cursor.moveToFirst()) {
            receiptNumbers = new ArrayList<>();
            orderNumbers = new ArrayList<>();
            amounts = new ArrayList<>();
            dates = new ArrayList<>();

            // Iterate through the cursor to retrieve the data
            do {
                String receiptNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_NUMBER));
                String orderNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_NUMBER));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_TOTAL));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT));

                // Add the data to the corresponding lists
                receiptNumbers.add(receiptNumber);
                orderNumbers.add(orderNumber);
                amounts.add(amount);
                dates.add(date);
            } while (cursor.moveToNext());

            // Close the cursor after use
            cursor.close();

            // Iterate through the data lists to populate the table rows
            for (int i = 0; i < receiptNumbers.size(); i++) {
                // Retrieve the values from the lists
                String receiptNumber = receiptNumbers.get(i);
                String orderNumber = orderNumbers.get(i);
                double amount = amounts.get(i);
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

                TextView amountTextView = createTextView(String.valueOf(amount), false);
                TextView dateTextView = createTextView(date, false);

                row.addView(amountTextView);
                row.addView(dateTextView);

                // Add the table row to the table layout
                tableLayout.addView(row);
            }
        }

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
                dataAccess.getTotalSales(startDate, endDate);
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

}
