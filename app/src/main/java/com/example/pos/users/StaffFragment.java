package com.example.pos.users;

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

public class StaffFragment extends Fragment {

    private TableLayout tableLayout;
    private DataAccess dataAccess;
    private Spinner itemCategorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private String itemName;

    public StaffFragment() {
        // Required empty public constructor
    }

    @SuppressLint("Range")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff, container, false);
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

        TextView usernameHeader = createTextView("Username", true);
        TextView fullNameHeader = createTextView("Full name", true);
        TextView statusHeader = createTextView("Status", true);
        TextView createdAtHeader = createTextView("Created at", true);

        headerRow.addView(usernameHeader);
        headerRow.addView(fullNameHeader);
        headerRow.addView(statusHeader);
        headerRow.addView(createdAtHeader);

        tableLayout.addView(headerRow);

        Cursor cursor = dataAccess.getStaffAccounts();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                TableRow tableRow = new TableRow(requireContext());

                TextView usernameTextView = createTextView(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME)), false);
                TextView fullNameTextView = createTextView(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FUll_NAME)), false);
                TextView statusTextView = createTextView(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS)), false);
                TextView createdAtTextView = createTextView(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_CREATED_AT)), false);

                tableRow.addView(usernameTextView);
                tableRow.addView(fullNameTextView);
                tableRow.addView(statusTextView);
                tableRow.addView(createdAtTextView);

                // Add OnClickListener to the row
                tableRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle row click event here
                        // You can access the username or other data using the cursor
                        String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME));
                        Toast.makeText(requireContext(), "Clicked on item: " + username, Toast.LENGTH_SHORT).show();
                    }
                });

                tableLayout.addView(tableRow);
            } while (cursor.moveToNext());

            cursor.close();
        }
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