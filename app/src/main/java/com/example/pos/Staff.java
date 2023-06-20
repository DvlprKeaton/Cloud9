package com.example.pos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pos.inventory.BeveragesFragment;
import com.example.pos.inventory.DisposablesFragment;
import com.example.pos.inventory.FoodFragment;
import com.example.pos.inventory.IngredientsFragment;
import com.example.pos.inventory.PastriesFragment;
import com.example.pos.users.AdminFragment;
import com.example.pos.users.StaffFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Staff extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private Button addButton, deleteButton;

    // Declare the spinner and its adapter
    private Spinner itemCategorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private DataAccess dataAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);


        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        setupViewPager();
        tabLayout.setupWithViewPager(viewPager);

        NavigationView navigationView = findViewById(R.id.leftNavigationView);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation item selection here
                int itemId = menuItem.getItemId();
                switch (itemId) {
                    case R.id.menu:
                        // Start the new activity
                        Intent intentMenu = new Intent(Staff.this, Menu.class);
                        startActivity(intentMenu);

                        // Finish the current activity
                        finish();
                        break;
                    case R.id.inventory:
                        // Start the new activity
                        Intent intentInventory = new Intent(Staff.this, Inventory.class);
                        startActivity(intentInventory);

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

        // Initialize the "Add" button
        addButton = findViewById(R.id.addButton);
        deleteButton = findViewById(R.id.deleteButton);
        dataAccess = new DataAccess(Staff.this);

        // Set click listener for the "Add" button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the pop-up modal
                showAddUserDialog();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the pop-up modal
                showDeleteConfirmationDialog();
            }
        });


    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Item");
        builder.setMessage("Are you sure you want to delete this item?");

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.delete_item_dialog, null);
        builder.setView(dialogView);

        // Find views in the custom layout
        Spinner itemNamesSpinner = dialogView.findViewById(R.id.itemNames);
        EditText quantityTextView = dialogView.findViewById(R.id.quantityTextView);
        Button decreaseButton = dialogView.findViewById(R.id.decreaseButton);
        Button increaseButton = dialogView.findViewById(R.id.increaseButton);

        // Set initial quantity
        final int[] quantity = {1};
        quantityTextView.setText(String.valueOf(quantity[0]));

        // Set click listeners for the decrease and increase buttons
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    quantityTextView.setText(String.valueOf(quantity[0]));
                }
            }
        });

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity[0]++;
                quantityTextView.setText(String.valueOf(quantity[0]));
            }
        });


        // Retrieve unique item names from the database
        List<String> uniqueItemNames = dataAccess.getUniqueItemNames();

        // Set up the adapter for the item names spinner
        ArrayAdapter<String> itemNamesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, uniqueItemNames);
        itemNamesSpinner.setAdapter(itemNamesAdapter);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the delete action
                String selectedItem = itemNamesSpinner.getSelectedItem().toString();
                String quantityString = quantityTextView.getText().toString();
                int quantity = Integer.parseInt(quantityString);

                // Delete the selected item from the database
                dataAccess.deleteInventoryItems(selectedItem, quantity, Staff.this);

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel the delete action
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void showAddUserDialog() {
        // Create a Dialog instance
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_user_dialog);
        dialog.setCancelable(true);

        // Find the UI elements in the dialog layout
        EditText fullNameEditText = dialog.findViewById(R.id.fullNameEditText);
        EditText usernameEditText = dialog.findViewById(R.id.usernameEditText);
        EditText passwordEditText = dialog.findViewById(R.id.passwordEditText);
        EditText confirmPasswordEditText = dialog.findViewById(R.id.conpasswordEditText);
        Spinner itemCategorySpinner = dialog.findViewById(R.id.itemCategorySpinner);
        Button addUserButton = dialog.findViewById(R.id.addUserButton);

        // Set up the adapter for the category spinner
        String[] categories = {"Admin", "Staff"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        itemCategorySpinner.setAdapter(categoryAdapter);

        // Set click listener for the "Add User" button in the dialog
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Add User" button click inside the dialog
                String fullName = fullNameEditText.getText().toString();
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();
                String category = itemCategorySpinner.getSelectedItem().toString();

                // Validate input fields
                if (fullName.trim().isEmpty()) {
                    Toast.makeText(Staff.this, "Full Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (username.trim().isEmpty()) {
                    Toast.makeText(Staff.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.trim().isEmpty()) {
                    Toast.makeText(Staff.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(Staff.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create an instance of DataAccess
                DataAccess dataAccess = new DataAccess(Staff.this);

                // Call the insertUser method to add the user to the database
                long newRowId = dataAccess.insertUser(username, fullName, password, category, "active", getCurrentDateTime(), getCurrentDateTime());

                // Check the result of the insertion
                if (newRowId != -1) {
                    Toast.makeText(Staff.this, "User added to the database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Staff.this, "Failed to add user to the database", Toast.LENGTH_SHORT).show();
                }

                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }

    // Helper method to get the current date and time
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(System.currentTimeMillis()));
    }



    private void setupViewPager() {
        Staff.ViewPagerAdapter adapter = new Staff.ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new StaffFragment(), "Staff");
        adapter.addFragment(new AdminFragment(), "Admin");
        viewPager.setAdapter(adapter);
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> titles = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}