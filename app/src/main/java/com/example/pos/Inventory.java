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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pos.inventory.BeveragesFragment;
import com.example.pos.inventory.DisposablesFragment;
import com.example.pos.inventory.FoodFragment;
import com.example.pos.inventory.IngredientsFragment;
import com.example.pos.inventory.PastriesFragment;
import com.example.pos.menu.AddOnsFragment;
import com.example.pos.menu.EspressoFragment;
import com.example.pos.menu.FrappeFragment;
import com.example.pos.menu.FruitTeaFragment;
import com.example.pos.menu.NonEspressoFragment;
import com.example.pos.menu.SparklingAdeFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Inventory extends AppCompatActivity {

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
        setContentView(R.layout.activity_inventory);

        dataAccess = new DataAccess(Inventory.this);

        dataAccess.getAllItems(Inventory.this);

        SharedPreferences sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", "");
        String userRole = sharedPreferences.getString("userRole", "");

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();
        dataAccess.insertMovement(0, username, "Redirected to the Inventory", updatedAt);

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
                        Intent intentMenu = new Intent(Inventory.this, Menu.class);
                        startActivity(intentMenu);
                        Inventory.this.overridePendingTransition(0, 0); // Remove transition animation
                        Inventory.this.finish();

                        break;
                    case R.id.staff:
                        // Start the new activity
                        Intent intentStaff = new Intent(Inventory.this, Staff.class);
                        startActivity(intentStaff);
                        Inventory.this.overridePendingTransition(0, 0); // Remove transition animation
                        Inventory.this.finish();

                        break;
                    case R.id.reports:
                        // Start the new activity
                        Intent intentReports = new Intent(Inventory.this, Reports.class);
                        startActivity(intentReports);
                        Inventory.this.overridePendingTransition(0, 0); // Remove transition animation
                        Inventory.this.finish();

                        break;
                    case R.id.cash_drawer:
                        // Start the new activity
                        Intent intentCash = new Intent(Inventory.this, CashDrawer.class);
                        startActivity(intentCash);
                        Inventory.this.overridePendingTransition(0, 0); // Remove transition animation
                        Inventory.this.finish();

                        break;
                    case R.id.transaction:
                        // Start the new activity
                        Intent intentTransactions = new Intent(Inventory.this, Transactions.class);
                        startActivity(intentTransactions);
                        Inventory.this.overridePendingTransition(0, 0); // Remove transition animation
                        Inventory.this.finish();

                        break;
                    case R.id.settings:
                        // Start the new activity
                        Intent intentSettings = new Intent(Inventory.this, Settings.class);
                        startActivity(intentSettings);
                        Inventory.this.overridePendingTransition(0, 0); // Remove transition animation
                        Inventory.this.finish();

                        break;
                    case R.id.logout:
                        DataAccess dataAccess = new DataAccess(Inventory.this); // Initialize the DataAccess object
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

        // Initialize the "Add" button
        addButton = findViewById(R.id.addButton);
        deleteButton = findViewById(R.id.deleteButton);


        // Set click listener for the "Add" button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the pop-up modal
                showAddItemDialog();
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
                dataAccess.deleteInventoryItems(selectedItem, quantity, Inventory.this);

                Intent intentMenu = new Intent(Inventory.this, Inventory.class);
                startActivity(intentMenu);
                Inventory.this.overridePendingTransition(0, 0); // Remove transition animation
                Inventory.this.finish();

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



    private void showAddItemDialog() {
        // Create a Dialog instance
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_item_dialog);
        dialog.setCancelable(true);

        // Find the UI elements in the dialog layout
        EditText itemNameEditText = dialog.findViewById(R.id.itemNameEditText);
        itemCategorySpinner = dialog.findViewById(R.id.itemCategorySpinner);
        LinearLayout quantityPickerLayout = dialog.findViewById(R.id.quantityPickerLayout);
        EditText quantityTextView = dialog.findViewById(R.id.quantityTextView);
        Button decreaseButton = dialog.findViewById(R.id.decreaseButton);
        Button increaseButton = dialog.findViewById(R.id.increaseButton);
        Button addItemButton = dialog.findViewById(R.id.addItemButton);
        Button cancel = dialog.findViewById(R.id.Cancel);

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

        // Set up the adapter for the category spinner
        String[] categories = {"Beverages", "Disposables", "Food", "Ingredients", "Pastries"};
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        itemCategorySpinner.setAdapter(categoryAdapter);

        // Set click listener for the "Add" button in the dialog
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Add" button click inside the dialog
                String itemName = itemNameEditText.getText().toString();
                String category = itemCategorySpinner.getSelectedItem().toString();
                String quantityString = quantityTextView.getText().toString();

                // Validate item name
                if (itemName.trim().isEmpty()) {
                    // Item name is empty, show an error message
                    Toast.makeText(Inventory.this, "Item name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert quantity to int
                int quantity = Integer.parseInt(quantityString);

                // Create an instance of DataAccess
                DataAccess dataAccess = new DataAccess(Inventory.this);


                // Call the insertItem method to add the item to the database
                long newRowId = dataAccess.insertItem(itemName, category, quantity);

                // Check the result of the insertion
                if (newRowId != -1) {
                    // Insertion was successful
                    Intent intentMenu = new Intent(Inventory.this, Inventory.class);
                    startActivity(intentMenu);
                    Inventory.this.overridePendingTransition(0, 0); // Remove transition animation
                    Inventory.this.finish();
                    Toast.makeText(Inventory.this, "Item added to the database", Toast.LENGTH_SHORT).show();
                } else {
                    // Insertion failed
                    Toast.makeText(Inventory.this, "Failed to add item to the database", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });



        // Show the dialog
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void setupViewPager() {
        Inventory.ViewPagerAdapter adapter = new Inventory.ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DisposablesFragment(), "Disposables");
        adapter.addFragment(new IngredientsFragment(), "Ingredients");
        adapter.addFragment(new PastriesFragment(), "Pastries");
        adapter.addFragment(new BeveragesFragment(), "Beverages");
        adapter.addFragment(new FoodFragment(), "Food");
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