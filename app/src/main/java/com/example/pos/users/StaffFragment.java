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
                TextView categoryTextView = createTextView(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY)), false);
                TextView idTextView = createTextView(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)), false);
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
                        String userID = idTextView.getText().toString();
                        String username = usernameTextView.getText().toString();
                        String fullname = fullNameTextView.getText().toString();
                        String category = categoryTextView.getText().toString();
                        showUpdateDialog(username,fullname,category,userID);
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

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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