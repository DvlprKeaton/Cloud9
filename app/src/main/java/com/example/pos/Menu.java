package com.example.pos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.example.pos.menu.AddOnsFragment;
import com.example.pos.menu.EspressoFragment;
import com.example.pos.menu.FrappeFragment;
import com.example.pos.menu.FruitTeaFragment;
import com.example.pos.menu.NonEspressoFragment;
import com.example.pos.menu.ShortOrdersFragment;
import com.example.pos.menu.SparklingAdeFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.Manifest;

public class Menu extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private DataAccess dataAccess;
    private Button addMenu;
    private DatabaseHelper dbHelper;
    private LinearLayout totalPriceMenu;
    public int orderNumber;
    double totalPrice;
    private LoadingScreenDialog loadingScreenDialog;
    private String final_orderType;
    private String final_paymentType;
    private String username;
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String SMS_SENDER = "GCash"; // Replace with the desired sender's phone number
    private static final int PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        addMenu = findViewById(R.id.addMenuButton);
        totalPriceMenu = findViewById(R.id.totalPriceLayout);

        loadingScreenDialog = new LoadingScreenDialog(this);
        // Show the loading screen
        //showLoadingScreen();

        // Register the SMSReceiver to listen for SMS_RECEIVED_ACTION
        SMSReceiver smsReceiver = new SMSReceiver();
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);


        // Perform your operations, such as loading data or starting a fragment

        setupViewPager();
        tabLayout.setupWithViewPager(viewPager);

        SharedPreferences sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

        // Create an instance of DatabaseHelper in your Application class
        dbHelper = new DatabaseHelper(getApplicationContext());

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        username = sharedPreferences.getString("username", "");
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

        totalPriceMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderNumber = dataAccess.orderNumber();
                totalPrice = dataAccess.getPendingTotal(String.valueOf(orderNumber));
                showConfirmOrderDialog();

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

        // Hide the loading screen
        //hideLoadingScreen();

    }



    private void setupViewPager() {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new EspressoFragment(), "ESPRESSO BASED");
        adapter.addFragment(new FruitTeaFragment(), "FRUIT TEA");
        adapter.addFragment(new NonEspressoFragment(), "NON-ESPRESSO");
        adapter.addFragment(new FrappeFragment(), "FRAPPE");
        adapter.addFragment(new SparklingAdeFragment(), "SPARKLING ADE");
        adapter.addFragment(new AddOnsFragment(), "ADD ONS");
        adapter.addFragment(new ShortOrdersFragment(), "SHORT ORDERS");
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

    private void showConfirmOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Summary of Order");
        builder.setMessage("Please confirm order before check out.");

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.confirm_order_dialog, null);
        builder.setView(dialogView);

        // Find views in the custom layout
        TextView transactionNumber = dialogView.findViewById(R.id.transactionNumber);
        TableLayout tableLayout = dialogView.findViewById(R.id.tableLayout);
        RadioGroup paymentType = dialogView.findViewById(R.id.radioGroupPaymentType);
        RadioGroup orderType = dialogView.findViewById(R.id.radioGroupOrderType);
        TextView total = dialogView.findViewById(R.id.totalPrice);
        Button confirm = dialogView.findViewById(R.id.Confirm);
        Button cancel = dialogView.findViewById(R.id.Cancel);
        Button clear = dialogView.findViewById(R.id.clearAll);


        transactionNumber.setText("Order #" + orderNumber);
        total.setText("Check Out Amount: \u20B1" + totalPrice);

        int paddingDp = 10; // Set the desired padding in dp
        float density = getResources().getDisplayMetrics().density;
        int paddingPx = (int) (paddingDp * density + 0.5f); // Convert dp to pixels

        int marginDp = 10; // Set the desired margin in dp
        int marginPx = (int) (marginDp * density + 0.5f); // Convert dp to pixels

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(marginPx, marginPx, marginPx, marginPx);

        // Create the first radio button
        RadioButton cash = new RadioButton(this);
        cash.setText("Cash");
        cash.setButtonDrawable(R.drawable.selector_radio_button_button);
        cash.setBackgroundResource(R.drawable.selector_radio_button_button);
        cash.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        cash.setLayoutParams(layoutParams);
        paymentType.addView(cash);

        // Create the second radio button
        RadioButton gCash = new RadioButton(this);
        gCash.setText("GCash");
        gCash.setButtonDrawable(R.drawable.selector_radio_button_button);
        gCash.setBackgroundResource(R.drawable.selector_radio_button_button);
        gCash.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        gCash.setLayoutParams(layoutParams);
        paymentType.addView(gCash);

        // Create the third radio button
        RadioButton dineIn = new RadioButton(this);
        dineIn.setText("Dine In");
        dineIn.setButtonDrawable(R.drawable.selector_radio_button_button);
        dineIn.setBackgroundResource(R.drawable.selector_radio_button_button);
        dineIn.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        dineIn.setLayoutParams(layoutParams);
        orderType.addView(dineIn);

        // Create the fourth radio button
        RadioButton takeOut = new RadioButton(this);
        takeOut.setText("Take Out");
        takeOut.setButtonDrawable(R.drawable.selector_radio_button_button);
        takeOut.setBackgroundResource(R.drawable.selector_radio_button_button);
        takeOut.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        takeOut.setLayoutParams(layoutParams);
        orderType.addView(takeOut);

        // Create the fifth radio button
        RadioButton delivery = new RadioButton(this);
        delivery.setText("Delivery");
        delivery.setButtonDrawable(R.drawable.selector_radio_button_button);
        delivery.setBackgroundResource(R.drawable.selector_radio_button_button);
        delivery.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        delivery.setLayoutParams(layoutParams);
        orderType.addView(delivery);

        tableLayout.removeAllViews();

        // Add table header row
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#CCCCCC"));

        TextView productNames = createTextView("Product name", true, Color.parseColor("#CCCCCC"));
        TextView quantityHeader = createTextView("Quantity", true, Color.parseColor("#CCCCCC"));
        TextView priceHeader = createTextView("Price", true, Color.parseColor("#CCCCCC"));

        headerRow.addView(productNames);
        headerRow.addView(quantityHeader);
        headerRow.addView(priceHeader);


        tableLayout.addView(headerRow);

        Cursor cursor = dataAccess.getPendingOrders(orderNumber);

        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, Double> itemPrices = dataAccess.getPendingOrderPrices(orderNumber); // Fetch the item prices

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PENDING_ORDER_NAME));

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
            TableRow tableRow = new TableRow(this);

            TextView itemNameTextView = createTextView(entry.getKey(), false, Color.parseColor("#CCCCCC"));
            TextView quantityTextView = createTextView(String.valueOf(entry.getValue()), false, Color.parseColor("#CCCCCC"));

            // Get the price for the current item
            double itemPrice = itemPrices.get(entry.getKey());

            TextView priceTextView = createTextView(String.valueOf(itemPrice), false, Color.parseColor("#CCCCCC"));

            tableRow.addView(itemNameTextView);
            tableRow.addView(quantityTextView);
            tableRow.addView(priceTextView);

            // Add OnClickListener to the row
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle row click event here
                }
            });

            tableLayout.addView(tableRow);

            itemNumber++;
            cursor.moveToNext();
        }

        cursor.close();



        // Add a click listener to the radio button
        cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the background drawable when clicked
                cash.setBackgroundResource(R.drawable.selector_radio_button_button_clicked);
                gCash.setBackgroundResource(R.drawable.selector_radio_button_button);

                final_paymentType = "Cash";
            }
        });

        gCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the background drawable when clicked
                gCash.setBackgroundResource(R.drawable.selector_radio_button_button_clicked);
                cash.setBackgroundResource(R.drawable.selector_radio_button_button);

                final_paymentType = "GCash";
            }
        });

        dineIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the background drawable when clicked
                dineIn.setBackgroundResource(R.drawable.selector_radio_button_button_clicked);
                takeOut.setBackgroundResource(R.drawable.selector_radio_button_button);
                delivery.setBackgroundResource(R.drawable.selector_radio_button_button);

                final_orderType = "dine in";
            }
        });

        takeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the background drawable when clicked
                takeOut.setBackgroundResource(R.drawable.selector_radio_button_button_clicked);
                dineIn.setBackgroundResource(R.drawable.selector_radio_button_button);
                delivery.setBackgroundResource(R.drawable.selector_radio_button_button);

                final_orderType = "take out";
            }
        });

        delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the background drawable when clicked
                delivery.setBackgroundResource(R.drawable.selector_radio_button_button_clicked);
                dineIn.setBackgroundResource(R.drawable.selector_radio_button_button);
                takeOut.setBackgroundResource(R.drawable.selector_radio_button_button);

                final_orderType = "delivery";
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // Prevent dialog dismissal on touch outside
        dialog.setCancelable(false); // Prevent dialog cancellation on back press
        dialog.show();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Menu.this, "" + final_paymentType, Toast.LENGTH_SHORT).show();
                dataAccess.updatePendingOrder(orderNumber, final_orderType, final_paymentType, username);
                if (final_paymentType.equals("GCash")){
                    showGcashCheckOutDialog();
                    dialog.dismiss();
                } else if (final_paymentType.equals("Cash")) {
                    showCheckOutDialog();
                    dialog.dismiss();
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataAccess.clearOrders();
                totalPrice = 0;
                //showLoadingScreen();
                recreate();
            }
        });



        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the negative button click
                // ...
                dialog.dismiss(); // Close the dialog
            }
        });


        dialog.show();


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
                    recreate();
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



    private void showCheckOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Check out in cash");
        builder.setMessage("Please check the given cash upon check out.");

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.check_out_dialog, null);
        builder.setView(dialogView);

        // Find views in the custom layout
        TextView transactionNumber = dialogView.findViewById(R.id.transactionNumber);
        TableLayout tableLayout = dialogView.findViewById(R.id.tableLayout);
        EditText payment = dialogView.findViewById(R.id.payment);
        TextView total = dialogView.findViewById(R.id.totalPrice);
        Button confirm = dialogView.findViewById(R.id.Confirm);
        Button cancel = dialogView.findViewById(R.id.Cancel);

        transactionNumber.setText("Order #" + orderNumber);
        total.setText("Check Out Amount: \u20B1" + totalPrice);

        tableLayout.removeAllViews();

        // Add table header row
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#CCCCCC"));

        TextView productNames = createTextView("Product name", true, Color.parseColor("#CCCCCC"));
        TextView quantityHeader = createTextView("Quantity", true, Color.parseColor("#CCCCCC"));
        TextView priceHeader = createTextView("Price", true, Color.parseColor("#CCCCCC"));

        headerRow.addView(productNames);
        headerRow.addView(quantityHeader);
        headerRow.addView(priceHeader);

        tableLayout.addView(headerRow);

        Cursor cursor = dataAccess.getPendingOrders(orderNumber);

        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, Double> itemPrices = dataAccess.getPendingOrderPrices(orderNumber); // Fetch the item prices

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PENDING_ORDER_NAME));

            if (itemQuantities.containsKey(itemName)) {
                int currentQuantity = itemQuantities.get(itemName);
                itemQuantities.put(itemName, currentQuantity + 1);
            } else {
                itemQuantities.put(itemName, 1);
            }
        }

        cursor.moveToFirst();

        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            TableRow tableRow = new TableRow(this);

            TextView itemNameTextView = createTextView(entry.getKey(), false, Color.parseColor("#CCCCCC"));
            TextView quantityTextView = createTextView(String.valueOf(entry.getValue()), false, Color.parseColor("#CCCCCC"));

            // Get the price for the current item
            double itemPrice = itemPrices.get(entry.getKey());

            TextView priceTextView = createTextView(String.valueOf(itemPrice), false, Color.parseColor("#CCCCCC"));

            tableRow.addView(itemNameTextView);
            tableRow.addView(quantityTextView);
            tableRow.addView(priceTextView);

            // Add OnClickListener to the row
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle row click event here
                }
            });

            tableLayout.addView(tableRow);

            cursor.moveToNext();
        }

        cursor.close();

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // Prevent dialog dismissal on touch outside
        dialog.setCancelable(false); // Prevent dialog cancellation on back press
        dialog.show();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double totalPayment = Double.parseDouble(payment.getText().toString().trim());
                if (totalPrice <= totalPayment) {
                    Toast.makeText(Menu.this, "" + totalPrice + " " + totalPayment, Toast.LENGTH_SHORT).show();
                    double change = totalPayment - totalPrice;
                    dataAccess.cashCheckOut(orderNumber, totalPayment, change, username, getApplicationContext());
                    showReceipt();
                    totalPrice = 0;
                    dialog.dismiss();
                } else {
                    Toast.makeText(Menu.this, "Invalid Amount", Toast.LENGTH_SHORT).show();
                }
            }
        });



        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });




        // Disable the Confirm button initially
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        // Enable or disable the Confirm button based on payment amount validity
        payment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                String paymentText = s.toString().trim();
                double totalPayment = paymentText.isEmpty() ? 0 : Double.parseDouble(paymentText);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(totalPrice <= totalPayment);
            }
        });
    }

    private void showGcashCheckOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Check out in GCash");
        builder.setMessage("Please check the given GCash payment upon check out.");

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.check_out_gcash_dialog, null);
        builder.setView(dialogView);

        // Find views in the custom layout
        TextView transactionNumber = dialogView.findViewById(R.id.transactionNumber);
        TableLayout tableLayout = dialogView.findViewById(R.id.tableLayout);
        EditText payment = dialogView.findViewById(R.id.payment);
        TextView total = dialogView.findViewById(R.id.totalPrice);
        Button confirm = dialogView.findViewById(R.id.Confirm);
        Button cancel = dialogView.findViewById(R.id.Cancel);

        transactionNumber.setText("Order #" + orderNumber);
        total.setText("Check Out Amount: \u20B1" + totalPrice);

        tableLayout.removeAllViews();

        // Add table header row
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#CCCCCC"));

        TextView productNames = createTextView("Product name", true, Color.parseColor("#CCCCCC"));
        TextView quantityHeader = createTextView("Quantity", true, Color.parseColor("#CCCCCC"));
        TextView priceHeader = createTextView("Price", true, Color.parseColor("#CCCCCC"));

        headerRow.addView(productNames);
        headerRow.addView(quantityHeader);
        headerRow.addView(priceHeader);

        tableLayout.addView(headerRow);

        Cursor cursor = dataAccess.getPendingOrders(orderNumber);

        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, Double> itemPrices = dataAccess.getPendingOrderPrices(orderNumber); // Fetch the item prices

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PENDING_ORDER_NAME));

            if (itemQuantities.containsKey(itemName)) {
                int currentQuantity = itemQuantities.get(itemName);
                itemQuantities.put(itemName, currentQuantity + 1);
            } else {
                itemQuantities.put(itemName, 1);
            }
        }

        cursor.moveToFirst();

        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            TableRow tableRow = new TableRow(this);

            TextView itemNameTextView = createTextView(entry.getKey(), false, Color.parseColor("#CCCCCC"));
            TextView quantityTextView = createTextView(String.valueOf(entry.getValue()), false, Color.parseColor("#CCCCCC"));

            // Get the price for the current item
            double itemPrice = itemPrices.get(entry.getKey());

            TextView priceTextView = createTextView(String.valueOf(itemPrice), false, Color.parseColor("#CCCCCC"));

            tableRow.addView(itemNameTextView);
            tableRow.addView(quantityTextView);
            tableRow.addView(priceTextView);

            // Add OnClickListener to the row
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle row click event here
                }
            });

            tableLayout.addView(tableRow);

            cursor.moveToNext();
        }

        cursor.close();

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // Prevent dialog dismissal on touch outside
        dialog.setCancelable(false); // Prevent dialog cancellation on back press
        dialog.show();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double totalPayment = Double.parseDouble(payment.getText().toString().trim());
                if (totalPrice <= totalPayment) {
                    Toast.makeText(Menu.this, "" + totalPrice + " " + totalPayment, Toast.LENGTH_SHORT).show();
                    double change = totalPayment - totalPrice;
                    dataAccess.cashCheckOut(orderNumber, totalPayment, change, username, getApplicationContext());
                    showReceipt();
                    totalPrice = 0;
                    dialog.dismiss();
                } else {
                    Toast.makeText(Menu.this, "Invalid Amount", Toast.LENGTH_SHORT).show();
                }
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Register a broadcast receiver to refresh the dialog when a message is received from GCash
        BroadcastReceiver smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // Initialize the dataAccess object with the Application context
                dataAccess = new DataAccess(context);

                if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        Object[] pdus = (Object[]) extras.get("pdus");
                        if (pdus != null) {
                            for (Object pdu : pdus) {
                                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                                String sender = sms.getOriginatingAddress();
                                String message = sms.getMessageBody();
                                //Toast.makeText(context, "" + sms, Toast.LENGTH_SHORT).show();
                                if (sender.equals(SMS_SENDER) && message.startsWith("You have received")) {
                                    String receivedDate = sms.getTimestampMillis() + "";
                                    // Find the starting position of "from "
                                    int startIndex = message.indexOf("from ") + 5;
                                    // Find the ending position of " w/"
                                    int endIndex = message.indexOf(" w/");
                                    // Extract the sender information
                                    String senderInfo = message.substring(startIndex, endIndex);
                                    // Split the sender information into name and number
                                    String[] parts = senderInfo.split("\\.");
                                    // Remove spaces and asterisks from the name
                                    String senderName = parts[0].trim();
                                    // Remove spaces from the number
                                    String senderNumber = parts[1].trim();

                                    int refNoIndex = message.indexOf("Ref. No.");
                                    String referenceNumber = message.substring(refNoIndex + 8).trim();

                                    int amountIndex = message.indexOf("PHP ");
                                    int amountEndIndex = message.indexOf(" of GCash");
                                    String receivedAmount = message.substring(amountIndex + 4, amountEndIndex).trim();
                                    double fin_receivedAmount = Double.parseDouble(receivedAmount);
                                    //Toast.makeText(context, "" + receivedDate + " The Amount is: " + receivedAmount + " From " + senderName + " " + senderNumber + " with the reference no. " + referenceNumber, Toast.LENGTH_SHORT).show();
                                    dataAccess.insertGCash(referenceNumber, senderName, senderNumber,fin_receivedAmount,receivedDate, message, transactionNumber.getText().toString(), String.valueOf(orderNumber), username,context);

                                    payment.setText(receivedAmount);
                                    //Toast.makeText(context, "" + receivedDate + " The Amount is: " + receivedAmount + " From " + senderName + " " + senderNumber + " with the reference no. " + referenceNumber, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            }
        };
        String orderNumberString = String.valueOf(orderNumber);

        // Create an intent filter with the desired action
        IntentFilter intentFilter = new IntentFilter(SMSReceiver.SMS_RECEIVED_ACTION);

        // Register the broadcast receiver with the intent filter
        registerReceiver(smsReceiver, intentFilter);

        // Disable the Confirm button initially
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        // Enable or disable the Confirm button based on payment amount validity
        payment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                String paymentText = s.toString().trim();
                double totalPayment = paymentText.isEmpty() ? 0 : Double.parseDouble(paymentText);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(totalPrice <= totalPayment);
            }
        });
    }

    @SuppressLint("Range")
    private void showReceipt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.receipt_confimation_dialog, null);
        builder.setView(dialogView);

        // Find views in the custom layout
        TextView transactionNumberTextView = dialogView.findViewById(R.id.transactionNumber);
        TextView receiptNumberTextView = dialogView.findViewById(R.id.receiptNumber);
        TextView totalPriceTextView = dialogView.findViewById(R.id.totalPrice);
        TextView totalPaymentTextView = dialogView.findViewById(R.id.totalPayment);
        TextView totalChangeTextView = dialogView.findViewById(R.id.totalChange);
        TableLayout tableLayout = dialogView.findViewById(R.id.tableLayout);
        Button print = dialogView.findViewById(R.id.Print);

        transactionNumberTextView.setText("Order #" + orderNumber);

        tableLayout.removeAllViews();

        // Add table header row
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#CCCCCC"));

        TextView productNames = createTextView("Product name", true, Color.parseColor("#CCCCCC"));
        TextView quantityHeader = createTextView("Quantity", true, Color.parseColor("#CCCCCC"));
        TextView priceHeader = createTextView("Price", true, Color.parseColor("#CCCCCC"));

        headerRow.addView(productNames);
        headerRow.addView(quantityHeader);
        headerRow.addView(priceHeader);

        tableLayout.addView(headerRow);

        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, Double> itemPrices = dataAccess.getOrdersPrices(orderNumber); // Fetch the item prices

        Cursor cursor = dataAccess.getReceipt(orderNumber);
        Cursor orderCursor = dataAccess.getOrders(orderNumber);

        while (orderCursor.moveToNext()) {
            String itemName = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_NAME));

            if (itemQuantities.containsKey(itemName)) {
                int currentQuantity = itemQuantities.get(itemName);
                itemQuantities.put(itemName, currentQuantity + 1);
            } else {
                itemQuantities.put(itemName, 1);
            }
        }

        cursor.moveToFirst();
        orderCursor.moveToFirst();

        String receiptId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ID));
        String receiptNumber = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_NUMBER));
        String orderQuantity = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_QUANTITY));
        String orderType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_TYPE));
        String orderDiscount = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_DISCOUNT));
        String orderDiscountType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_DISCOUNT_TYPE));
        String orderPaymentType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_PAYMENT_TYPE));
        String orderPayment = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_PAYMENT));
        String orderTotal = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_TOTAL));
        String orderChange = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_CHANGE));
        String totalPaymentValue = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_PAYMENT));
        String addedBy = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_BY));
        String addedAt = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_ORDER_ADDED_AT));

        receiptNumberTextView.setText(receiptNumber);
        totalPriceTextView.setText("₱"+ orderTotal);
        totalPaymentTextView.setText("₱"+ orderPayment);
        totalChangeTextView.setText("₱"+ orderChange);

        int itemNumber = 1;

        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            TableRow tableRow = new TableRow(this);

            TextView itemNameTextView = createTextView(entry.getKey(), false, Color.parseColor("#CCCCCC"));
            TextView quantityTextView = createTextView(String.valueOf(orderQuantity), false, Color.parseColor("#CCCCCC"));

            // Get the price for the current item
            double itemPrice = itemPrices.get(entry.getKey());
            Log.d("itemPriceMenu", String.valueOf(itemPrice) + orderQuantity);

            TextView priceTextView = createTextView(String.valueOf(itemPrice), false, Color.parseColor("#CCCCCC"));

            tableRow.addView(itemNameTextView);
            tableRow.addView(quantityTextView);
            tableRow.addView(priceTextView);


            // Add OnClickListener to the row
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle row click event here
                }
            });

            tableLayout.addView(tableRow);

            itemNumber++;
            cursor.moveToNext();
        }

        cursor.close();


        AlertDialog dialog = builder.create();
        dialog.show();

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Menu.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Storage permission not granted, request it
                    requestStoragePermission();
                } else {
                    // Storage permission granted, proceed with capturing and saving the image
                    print.setVisibility(View.GONE); // Hide the print button

                    View rootView = dialogView.getRootView();

                    // Create a bitmap of the rootView
                    rootView.setDrawingCacheEnabled(true);
                    Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
                    rootView.setDrawingCacheEnabled(false);

                    print.setVisibility(View.VISIBLE); // Show the print button again

                    int receipt_no = dataAccess.receiptNumber();
                    int order_no = dataAccess.orderNumber();

                    LocalDateTime currentDateTime = LocalDateTime.now();
                    String date_in = currentDateTime.toString();

                    String image_name = receipt_no + "-" + order_no + "-" + date_in;

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, image_name + ".png");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                    Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    OutputStream outputStream = null;

                    try {
                        outputStream = getContentResolver().openOutputStream(imageUri);
                        if (outputStream != null) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            outputStream.close();
                            Toast.makeText(Menu.this, "Receipt saved as image", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Menu.this, "Failed to save receipt image", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }



    private List<String> getMenuCategories() {
        List<String> menuCategories = new ArrayList<>();
        menuCategories.add("Espresso");
        menuCategories.add("Add Ons");
        menuCategories.add("Frappe");
        menuCategories.add("Fruit Tea");
        menuCategories.add("Non Espresso");
        menuCategories.add("Sparkling Ade");
        menuCategories.add("Short Orders");
        return menuCategories;
    }

    // Create a TextView with the specified text and optional background color
    private TextView createTextView(String text, boolean isHeader, int backgroundColor) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextColor(Color.BLACK);
        if (isHeader) {
            textView.setBackgroundColor(backgroundColor);
        }
        return textView;
    }

    private void showLoadingScreen() {
        loadingScreenDialog.show();
    }

    private void hideLoadingScreen() {
        if (loadingScreenDialog != null && loadingScreenDialog.isShowing()) {
            loadingScreenDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure to dismiss the loading screen dialog when the activity is destroyed
        hideLoadingScreen();
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Explain why the permission is needed
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Request");
            builder.setMessage("This app requires storage permission to save the receipt image. Please grant the permission to continue.");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Request the permission
                    ActivityCompat.requestPermissions(Menu.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                }
            });

            builder.show();
        } else {
            // Request the permission without explaining
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private Bitmap captureNestedScrollView(NestedScrollView nestedScrollView) {
        int width = nestedScrollView.getWidth();
        int height = 0;
        for (int i = 0; i < nestedScrollView.getChildCount(); i++) {
            height += nestedScrollView.getChildAt(i).getHeight();
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        nestedScrollView.draw(canvas);
        return bitmap;
    }





}