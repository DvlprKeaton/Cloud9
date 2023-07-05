package com.example.pos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.example.pos.R;
import com.example.pos.menu.AddOnsFragment;
import com.example.pos.menu.EspressoFragment;
import com.example.pos.menu.FrappeFragment;
import com.example.pos.menu.FruitTeaFragment;
import com.example.pos.menu.NonEspressoFragment;
import com.example.pos.menu.SparklingAdeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Menu extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private DataAccess dataAccess;
    private Button addMenu;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        addMenu = findViewById(R.id.addMenuButton);

        setupViewPager();
        tabLayout.setupWithViewPager(viewPager);

        SharedPreferences sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

        // Create an instance of DatabaseHelper in your Application class
        dbHelper = new DatabaseHelper(getApplicationContext());

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", "");
        String userRole = sharedPreferences.getString("userRole", "");

        NavigationView navigationView = findViewById(R.id.leftNavigationView);

        // Initialize the dataAccess object with the Application context
        dataAccess = new DataAccess(getApplicationContext());

        // Set click listener for the "Add" button
        addMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the pop-up modal
                showAddMenuDialog();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation item selection here
                int itemId = menuItem.getItemId();
                switch (itemId) {
                    case R.id.inventory:
                        // Start the new activity
                        Intent intentInventory = new Intent(Menu.this, Inventory.class);
                        startActivity(intentInventory);

                        // Finish the current activity
                        finish();
                        break;
                    case R.id.staff:
                        // Start the new activity
                        Intent intentStaff = new Intent(Menu.this, Staff.class);
                        startActivity(intentStaff);

                        // Finish the current activity
                        finish();
                        break;
                    case R.id.logout:
                        DataAccess dataAccess = new DataAccess(Menu.this); // Initialize the DataAccess object
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



    }



    private void setupViewPager() {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new EspressoFragment(), "ESPRESSO BASED");
        adapter.addFragment(new FruitTeaFragment(), "FRUIT TEA");
        adapter.addFragment(new NonEspressoFragment(), "NON-ESPRESSO");
        adapter.addFragment(new FrappeFragment(), "FRAPPE");
        adapter.addFragment(new SparklingAdeFragment(), "SPARKLING ADE");
        adapter.addFragment(new AddOnsFragment(), "ADD ONS");
        viewPager.setAdapter(adapter);
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
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

    private void showAddMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Menu");
        builder.setMessage("Enter the menu details:");

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_menu_dialog, null);
        builder.setView(dialogView);

        // Find views in the custom layout
        EditText menuNameEditText = dialogView.findViewById(R.id.menuNameEditText);
        EditText menuPriceEditText = dialogView.findViewById(R.id.menuPriceEditText);
        Spinner menuCategorySpinner = dialogView.findViewById(R.id.menuCategorySpinner);
        RadioButton radioButtonHot = dialogView.findViewById(R.id.radioButtonHot);
        RadioButton radioButtonCold = dialogView.findViewById(R.id.radioButtonCold);

        // Add Button
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String menuName = menuNameEditText.getText().toString();
                String menuPrice = menuPriceEditText.getText().toString();
                String menuCategory = menuCategorySpinner.getSelectedItem().toString();
                boolean isHot = radioButtonHot.isChecked();
                String category;

                if (menuCategory.equals("Espresso") || menuCategory.equals("Non Espresso")) {
                    if (isHot) {
                        category = "H " + menuCategory.trim();
                    } else {
                        category = "C " + menuCategory.trim();
                    }
                } else {
                    category = menuCategory.trim();
                }

                // Call the insertItem method to add the item to the database
                long newRowId = dataAccess.insertMenu(menuName, category, menuPrice);

                // Check the result of the insertion
                if (newRowId != -1) {
                    // Insertion was successful
                    Toast.makeText(Menu.this, "Menu added to the database", Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); // Close the dialog
                } else {
                    // Insertion failed
                    Toast.makeText(Menu.this, "Failed to add Menu to the database", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up the adapter for the menu category spinner
        List<String> menuCategories = getMenuCategories();
        ArrayAdapter<String> menuCategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, menuCategories);
        menuCategorySpinner.setAdapter(menuCategoryAdapter);

        // Set the initial state of radio buttons
        radioButtonHot.setEnabled(false);
        radioButtonCold.setEnabled(false);

        // Add a selection listener to the menu category spinner
        menuCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = menuCategories.get(position);
                if (selectedCategory.equals("Espresso") || selectedCategory.equals("Non Espresso")) {
                    radioButtonHot.setEnabled(true);
                    radioButtonCold.setEnabled(true);
                } else {
                    radioButtonHot.setEnabled(false);
                    radioButtonCold.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Close the dialog
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }


    private List<String> getMenuCategories() {
        List<String> menuCategories = new ArrayList<>();
        menuCategories.add("Espresso");
        menuCategories.add("Add Ons");
        menuCategories.add("Frappe");
        menuCategories.add("Fruit Tea");
        menuCategories.add("Non Espresso");
        menuCategories.add("Sparkling Ade");
        return menuCategories;
    }


}