package com.example.pos.inventory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pos.DataAccess;
import com.example.pos.DatabaseHelper;
import com.example.pos.R;

import java.util.HashMap;
import java.util.Map;


public class PastriesFragment extends Fragment {

    private TableLayout tableLayout;
    private DataAccess dataAccess;
    private Spinner itemCategorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private String itemName;
    public PastriesFragment() {
        // Required empty public constructor
    }

    @SuppressLint("Range")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pastries, container, false);
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

    @SuppressLint("Range")
    private void refreshInventoryItems() {
        tableLayout.removeAllViews();

        // Add table header row
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setBackgroundColor(Color.parseColor("#CCCCCC"));

        TextView itemNumberHeader = createTextView("Item #", true);
        TextView itemNameHeader = createTextView("Item Name", true);
        TextView quantityHeader = createTextView("Quantity", true);
        TextView dateInHeader = createTextView("Date In", true);

        headerRow.addView(itemNumberHeader);
        headerRow.addView(itemNameHeader);
        headerRow.addView(quantityHeader);
        headerRow.addView(dateInHeader);

        tableLayout.addView(headerRow);

        Cursor cursor = dataAccess.getPastriesItem();

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

            TextView itemNumberTextView = createTextView(String.valueOf(itemNumber), false);
            TextView itemNameTextView = createTextView(entry.getKey(), false);
            TextView quantityTextView = createTextView(String.valueOf(entry.getValue()), false);
            TextView dateInTextView = createTextView(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_DATE_IN)), false);

            tableRow.addView(itemNumberTextView);
            tableRow.addView(itemNameTextView);
            tableRow.addView(quantityTextView);
            tableRow.addView(dateInTextView);

            // Add OnClickListener to the row
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle row click event here
                    itemName = itemNameTextView.getText().toString();
                    showUpdateDialog(itemName);
                    Toast.makeText(requireContext(), "Clicked on item: " + itemName, Toast.LENGTH_SHORT).show();
                }
            });

            tableLayout.addView(tableRow);

            itemNumber++;
            cursor.moveToNext();
        }

        cursor.close();
    }

    private TextView createTextView(String text, boolean isHeader) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(16, 16, 16, 16);

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
        // Add more views as needed


        // Set up the adapter for the category spinner
        String[] categories = {"Beverages", "Disposables", "Food", "Ingredients", "Pastries"};
        categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        itemCategorySpinner.setAdapter(categoryAdapter);

        // Set the current item details in the dialog
        itemNameEditText.setText(itemName);
        // Set the current category or other details

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the update action
                String updatedItemName = itemNameEditText.getText().toString();
                String category = itemCategorySpinner.getSelectedItem().toString();

                if (updatedItemName.trim().isEmpty()) {
                    // Item name is empty, show an error message
                    Toast.makeText(getContext(), "Item name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the item in the database
                String previousItemName = itemName.toString();
                dataAccess.updateInventoryItem(previousItemName, updatedItemName, category, getContext());

                // Refresh the inventory items after updating
                refreshInventoryItems();
            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel the update action
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}