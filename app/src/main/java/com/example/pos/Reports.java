package com.example.pos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.pos.reports.InventoryReportsFragment;
import com.example.pos.reports.OrdersReportsFragment;
import com.example.pos.reports.ReceiptReportsFragment;
import com.example.pos.reports.SalesReportsFragment;
import com.example.pos.users.AdminFragment;
import com.example.pos.users.StaffFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Reports extends AppCompatActivity {

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
        setContentView(R.layout.activity_reports);

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
                        Intent intentMenu = new Intent(Reports.this, Menu.class);
                        startActivity(intentMenu);
                        Reports.this.overridePendingTransition(0, 0); // Remove transition animation
                        Reports.this.finish();

                        break;
                    case R.id.inventory:
                        // Start the new activity
                        Intent intentInventory = new Intent(Reports.this, Inventory.class);
                        startActivity(intentInventory);
                        Reports.this.overridePendingTransition(0, 0); // Remove transition animation
                        Reports.this.finish();

                        break;
                    case R.id.staff:
                        // Start the new activity
                        Intent intentStaff = new Intent(Reports.this, Staff.class);
                        startActivity(intentStaff);
                        Reports.this.overridePendingTransition(0, 0); // Remove transition animation
                        Reports.this.finish();

                        break;
                    case R.id.cash_drawer:
                        // Start the new activity
                        Intent intentCash = new Intent(Reports.this, CashDrawer.class);
                        startActivity(intentCash);
                        Reports.this.overridePendingTransition(0, 0); // Remove transition animation
                        Reports.this.finish();

                        break;
                    case R.id.transaction:
                        // Start the new activity
                        Intent intentTransactions = new Intent(Reports.this, Transactions.class);
                        startActivity(intentTransactions);
                        Reports.this.overridePendingTransition(0, 0); // Remove transition animation
                        Reports.this.finish();

                        break;
                    case R.id.settings:
                        // Start the new activity
                        Intent intentSettings = new Intent(Reports.this, Settings.class);
                        startActivity(intentSettings);
                        Reports.this.overridePendingTransition(0, 0); // Remove transition animation
                        Reports.this.finish();

                        break;
                    case R.id.logout:
                        DataAccess dataAccess = new DataAccess(Reports.this); // Initialize the DataAccess object
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

        dataAccess = new DataAccess(Reports.this);

        dataAccess.getAllItems(Reports.this);

        SharedPreferences sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", "");
        String userRole = sharedPreferences.getString("userRole", "");

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();
        dataAccess.insertMovement(0, username, "Redirected to the Reports", updatedAt);

    }


    private void setupViewPager() {
        Reports.ViewPagerAdapter adapter = new Reports.ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OrdersReportsFragment(), "Orders");
        adapter.addFragment(new ReceiptReportsFragment(), "Receipt");
        adapter.addFragment(new SalesReportsFragment(), "Sales");
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