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

import com.example.pos.reports.OrdersReportsFragment;
import com.example.pos.reports.ReceiptReportsFragment;
import com.example.pos.reports.SalesReportsFragment;
import com.example.pos.settings.BusinessSettingsFragment;
import com.example.pos.settings.ProfileSettingsFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Settings extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private Button addButton, deleteButton;

    private DataAccess dataAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
                        Intent intentMenu = new Intent(Settings.this, Menu.class);
                        startActivity(intentMenu);
                        Settings.this.overridePendingTransition(0, 0); // Remove transition animation
                        Settings.this.finish();

                        break;
                    case R.id.inventory:
                        // Start the new activity
                        Intent intentInventory = new Intent(Settings.this, Inventory.class);
                        startActivity(intentInventory);
                        Settings.this.overridePendingTransition(0, 0); // Remove transition animation
                        Settings.this.finish();

                        break;
                    case R.id.staff:
                        // Start the new activity
                        Intent intentStaff = new Intent(Settings.this, Staff.class);
                        startActivity(intentStaff);
                        Settings.this.overridePendingTransition(0, 0); // Remove transition animation
                        Settings.this.finish();

                        break;
                    case R.id.cash_drawer:
                        // Start the new activity
                        Intent intentCash = new Intent(Settings.this, CashDrawer.class);
                        startActivity(intentCash);
                        Settings.this.overridePendingTransition(0, 0); // Remove transition animation
                        Settings.this.finish();

                        break;
                    case R.id.transaction:
                        // Start the new activity
                        Intent intentTransactions = new Intent(Settings.this, Transactions.class);
                        startActivity(intentTransactions);
                        Settings.this.overridePendingTransition(0, 0); // Remove transition animation
                        Settings.this.finish();

                        break;
                    case R.id.reports:
                        // Start the new activity
                        Intent intentSettings = new Intent(Settings.this, Reports.class);
                        startActivity(intentSettings);
                        Settings.this.overridePendingTransition(0, 0); // Remove transition animation
                        Settings.this.finish();

                        break;
                    case R.id.logout:
                        DataAccess dataAccess = new DataAccess(Settings.this); // Initialize the DataAccess object
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

        dataAccess = new DataAccess(Settings.this);

        dataAccess.getAllItems(Settings.this);

        SharedPreferences sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", "");
        String userRole = sharedPreferences.getString("userRole", "");

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();
        dataAccess.insertMovement(0, username, "Redirected to the Settings", updatedAt);
    }

    private void setupViewPager() {
        Settings.ViewPagerAdapter adapter = new Settings.ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ProfileSettingsFragment(), "Profile");
        adapter.addFragment(new BusinessSettingsFragment(), "Business");
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
