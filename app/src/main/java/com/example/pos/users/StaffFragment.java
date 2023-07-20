package com.example.pos.users;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import com.example.pos.R;

import java.util.Arrays;
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

    private void addTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(getContext());
        headerRow.setBackgroundColor(getResources().getColor(R.color.primary));

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        TextView itemNumberHeaderTextView = new TextView(getContext());
        itemNumberHeaderTextView.setLayoutParams(params);
        itemNumberHeaderTextView.setText("Username");
        itemNumberHeaderTextView.setTextColor(getResources().getColor(R.color.black));
        itemNumberHeaderTextView.setGravity(Gravity.CENTER);
        itemNumberHeaderTextView.setPadding(10, 20, 10, 20);
        itemNumberHeaderTextView.setTextSize(16);
        itemNumberHeaderTextView.setTypeface(null, Typeface.BOLD); // Set text decoration to bold
        headerRow.addView(itemNumberHeaderTextView);

        TextView itemNameHeaderTextView = new TextView(getContext());
        itemNameHeaderTextView.setLayoutParams(params);
        itemNameHeaderTextView.setText("Fullname");
        itemNameHeaderTextView.setTextColor(getResources().getColor(R.color.black));
        itemNameHeaderTextView.setGravity(Gravity.CENTER);
        itemNameHeaderTextView.setPadding(10, 20, 10, 20);
        itemNameHeaderTextView.setTextSize(16);
        itemNameHeaderTextView.setTypeface(null, Typeface.BOLD); // Set text decoration to bold
        headerRow.addView(itemNameHeaderTextView);

        TextView quantityHeaderTextView = new TextView(getContext());
        quantityHeaderTextView.setLayoutParams(params);
        quantityHeaderTextView.setText("Status");
        quantityHeaderTextView.setTextColor(getResources().getColor(R.color.black));
        quantityHeaderTextView.setGravity(Gravity.CENTER);
        quantityHeaderTextView.setPadding(10, 20, 10, 20);
        quantityHeaderTextView.setTextSize(16);
        quantityHeaderTextView.setTypeface(null, Typeface.BOLD); // Set text decoration to bold
        headerRow.addView(quantityHeaderTextView);

        TextView dateInHeaderTextView = new TextView(getContext());
        dateInHeaderTextView.setLayoutParams(params);
        dateInHeaderTextView.setText("Date Created");
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

        Cursor cursor = dataAccess.getStaffAccounts();

        if (cursor != null && cursor.moveToFirst()) {
            int currentRow = 0;

            do {
                TableRow tableRow = new TableRow(requireContext());

                TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

                final String userID = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID));
                final String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME));
                final String fullname = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FUll_NAME));
                final String category = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY));

                TextView usernameTextView = createTextView(username, false);
                usernameTextView.setLayoutParams(params);

                TextView fullNameTextView = createTextView(fullname, false);
                fullNameTextView.setLayoutParams(params);

                TextView statusTextView = createTextView(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS)), false);
                statusTextView.setLayoutParams(params);
                statusTextView.setTextColor(getResources().getColor(R.color.black)); // Set the text color to black

                TextView createdAtTextView = createTextView(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_CREATED_AT)), false);
                createdAtTextView.setLayoutParams(params);

                tableRow.addView(usernameTextView);
                tableRow.addView(fullNameTextView);
                tableRow.addView(statusTextView);
                tableRow.addView(createdAtTextView);

                // Set background color for alternate rows
                if (currentRow % 2 == 0) {
                    tableRow.setBackgroundColor(getResources().getColor(R.color.row_even));
                } else {
                    tableRow.setBackgroundColor(getResources().getColor(R.color.row_odd));
                }

                // Add OnClickListener to the row
                tableRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(username, fullname, category, userID);
                    }
                });

                tableLayout.addView(tableRow);
                currentRow++;
            } while (cursor.moveToNext());

            cursor.close();
        }
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

    private void showUpdateDialog(String username, String fullname, String category,String userID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Update User");

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.update_user_dialog, null);
        builder.setView(dialogView);

        // Find views in the custom layout
        EditText userName = dialogView.findViewById(R.id.usernameEditText);
        EditText fullName = dialogView.findViewById(R.id.fullNameEditText);
        EditText password = dialogView.findViewById(R.id.passwordEditText);
        EditText conpassword = dialogView.findViewById(R.id.conpasswordEditText);
        itemCategorySpinner = dialogView.findViewById(R.id.itemCategorySpinner);
        Button updateUser = dialogView.findViewById(R.id.updateUser);
        Button cancel = dialogView.findViewById(R.id.Cancel);
        // Add more views as needed


        // Set up the adapter for the category spinner
        String[] categories = {"Staff", "Admin"};
        categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        itemCategorySpinner.setAdapter(categoryAdapter);

        // Set the current item details in the dialog
        userName.setHint(username);
        fullName.setHint(fullname);
        // Set the current category or other details
        // Set the selected category from the database
        String selectedCategory = category; // Replace this with the value retrieved from the database
        int selectedCategoryIndex = Arrays.asList(categories).indexOf(selectedCategory);
        itemCategorySpinner.setSelection(selectedCategoryIndex);


        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // Prevent dialog dismissal on touch outside
        dialog.setCancelable(false); // Prevent dialog cancellation on back press

        updateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the update action
                String updatedUserName = userName.getText().toString();
                String updatedFullName = fullName.getText().toString();
                String updatedPassword = password.getText().toString();
                String updatedConPass = conpassword.getText().toString();
                String category = itemCategorySpinner.getSelectedItem().toString();
                String selected = selectedCategory.trim();

                int updateUserID = Integer.parseInt(userID);

                if (updatedUserName.trim().isEmpty() && updatedFullName.trim().isEmpty() && updatedPassword.trim().isEmpty() && updatedConPass.trim().isEmpty() && category.equals(selected)) {
                    // Item name is empty, show an error message
                    Toast.makeText(getContext(), "No fields to update", Toast.LENGTH_SHORT).show();
                    return;
                }

                dataAccess.updateStaff(updatedUserName, updatedFullName, updatedPassword,updatedConPass, category,updateUserID, getContext());

                // Refresh the inventory items after updating
                refreshInventoryItems();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cancel the update action
                dialog.dismiss();
            }
        });


        dialog.show();
    }
}