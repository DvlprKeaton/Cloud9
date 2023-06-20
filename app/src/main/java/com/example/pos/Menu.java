package com.example.pos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
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
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class Menu extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private DataAccess dataAccess;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

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
}