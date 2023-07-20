package com.example.pos.inventory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pos.DataAccess;
import com.example.pos.DatabaseHelper;
import com.example.pos.Inventory;
import com.example.pos.R;

import java.util.HashMap;
import java.util.Map;

public class DisposablesFragment extends Fragment {

    private TableLayout tableLayout;
    private DataAccess dataAccess;
    private Spinner itemCategorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private String itemName;
    public DisposablesFragment() {
    }

    @SuppressLint("Range")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_disposables, container, false);
        tableLayout = rootView.findViewById(R.id.tableLayout);
        dataAccess = new DataAccess(requireContext());
        refreshInventoryItems();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshInventoryItems();
    }

    private void addTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(getContext());
        headerRow.setBackgroundColor(getResources().getColor(R.color.primary));

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        TextView itemNumberHeaderTextView = new TextView(getContext());
        itemNumberHeaderTextView.setLayoutParams(params);
        itemNumberHeaderTextView.setText("Item #");
        itemNumberHeaderTextView.setTextColor(getResources().getColor(R.color.black));
        itemNumberHeaderTextView.setGravity(Gravity.CENTER);
        itemNumberHeaderTextView.setPadding(10, 20, 10, 20);
        itemNumberHeaderTextView.setTextSize(16);
        itemNumberHeaderTextView.setTypeface(null, Typeface.BOLD); // Set text decoration to bold
        headerRow.addView(itemNumberHeaderTextView);

        TextView itemNameHeaderTextView = new TextView(getContext());
        itemNameHeaderTextView.setLayoutParams(params);
        itemNameHeaderTextView.setText("Item Name");
        itemNameHeaderTextView.setTextColor(getResources().getColor(R.color.black));
        itemNameHeaderTextView.setGravity(Gravity.CENTER);
        itemNameHeaderTextView.setPadding(10, 20, 10, 20);
        itemNameHeaderTextView.setTextSize(16);
        itemNameHeaderTextView.setTypeface(null, Typeface.BOLD); // Set text decoration to bold
        headerRow.addView(itemNameHeaderTextView);

        TextView quantityHeaderTextView = new TextView(getContext());
        quantityHeaderTextView.setLayoutParams(params);
        quantityHeaderTextView.setText("Quantity");
        quantityHeaderTextView.setTextColor(getResources().getColor(R.color.black));
        quantityHeaderTextView.setGravity(Gravity.CENTER);
        quantityHeaderTextView.setPadding(10, 20, 10, 20);
        quantityHeaderTextView.setTextSize(16);
        quantityHeaderTextView.setTypeface(null, Typeface.BOLD); // Set text decoration to bold
        headerRow.addView(quantityHeaderTextView);

        TextView dateInHeaderTextView = new TextView(getContext());
        dateInHeaderTextView.setLayoutParams(params);
        dateInHeaderTextView.setText("Date In");
        dateInHeaderTextView.setTextColor(getResources().getColor(R.color.black));
        dateInHeaderTextView.setGravity(Gravity.CENTER);
        dateInHeaderTextView.setPadding(10, 20, 10, 20);
        dateInHeaderTextView.setTextSize(16);
        dateInHeaderTextView.setTypeface(null, Typeface.BOLD); // Set text decoration to bold
        headerRow.addView(dateInHeaderTextView);

        tableLayout.addView(headerRow);
    }


    @SuppressLint("Range")
    private void refreshInventoryItems() {
        tableLayout.removeAllViews();

        // Add the table header
        addTableHeader(tableLayout);

        Cursor cursor = dataAccess.getDisposableItems();

        Map<String, Integer> itemQuantities = new HashMap<>();

        while (cursor.moveToNext()) {
            String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_NAME));

            if (itemQuantities.containsKey(itemName)) {
                int currentQuantity = itemQuantities.get(itemName);
                itemQuantities.put(itemName, currentQuantity + 1);
            } else {
                itemQuantities.put(itemName, 1);
            }
        }

        cursor.moveToFirst();

        int itemNumber = 1;

        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            TableRow tableRow = new TableRow(requireContext());

            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            TextView itemNumberTextView = createTextView(String.valueOf(itemNumber), false);
            itemNumberTextView.setLayoutParams(params);

            TextView itemNameTextView = createTextView(entry.getKey(), false);
            itemNameTextView.setLayoutParams(params);

            TextView quantityTextView = createTextView(String.valueOf(entry.getValue()), false);
            quantityTextView.setLayoutParams(params);

            TextView dateInTextView = createTextView(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_DATE_IN)), false);
            dateInTextView.setLayoutParams(params);

            tableRow.addView(itemNumberTextView);
            tableRow.addView(itemNameTextView);
            tableRow.addView(quantityTextView);
            tableRow.addView(dateInTextView);

            // Set background color for alternate rows
            if (itemNumber % 2 == 0) {
                tableRow.setBackgroundColor(getResources().getColor(R.color.row_odd));
            } else {
                tableRow.setBackgroundColor(getResources().getColor(R.color.row_even));
            }

            // Add OnClickListener to the row
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle row click event here
                    itemName = itemNameTextView.getText().toString();
                    showUpdateDialog(itemName);
                }
            });

            tableLayout.addView(tableRow);

            itemNumber++;
            cursor.moveToNext();
        }

        cursor.close();
    }


    private TextView createTextView(String text, boolean isHeader) {

        float scale = getResources().getDisplayMetrics().density;
        int paddingInPixels = (int) (10 * scale + 0.5f); // 10dp converted to pixels

        TextView textView = new TextView(requireContext());
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

    private void showUpdateDialog(String itemName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Update Item");

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.update_item_dialog, null);
        builder.setView(dialogView);

        // Find views in the custom layout
        EditText itemNameEditText = dialogView.findViewById(R.id.itemNameEditText);
        itemCategorySpinner = dialogView.findViewById(R.id.itemCategorySpinner);
        Button updateItem = dialogView.findViewById(R.id.updateItem);
        Button cancel = dialogView.findViewById(R.id.Cancel);
        // Add more views as needed


        // Set up the adapter for the category spinner
        String[] categories = {"Beverages", "Disposables", "Food", "Ingredients", "Pastries"};
        categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        itemCategorySpinner.setAdapter(categoryAdapter);
        itemCategorySpinner.setSelection(1);
        // Set the current item details in the dialog
        itemNameEditText.setText(itemName);
        // Set the current category or other details



        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // Prevent dialog dismissal on touch outside
        dialog.setCancelable(false); // Prevent dialog cancellation on back press

        updateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the update action
                String updatedItemName = itemNameEditText.getText().toString();
                String category = itemCategorySpinner.getSelectedItem().toString();

                if (updatedItemName.trim().isEmpty()) {
                    // Item name is empty, show an error message
                    Toast.makeText(getContext(), "Item name cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    // Update the item in the database
                    String previousItemName = itemName.toString();
                    dataAccess.updateInventoryItem(previousItemName, updatedItemName, category, getContext());

                    // Refresh the inventory items after updating
                    refreshInventoryItems();

                    dialog.dismiss();
                }
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }



    // Other fragment code...
}
