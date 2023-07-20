package com.example.pos;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import android.os.Environment;
import android.os.ParcelUuid;
import android.provider.MediaStore;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    String[] required_permissions = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA
    };

    boolean is_storage_image_permitted = false;
    boolean is_camera_access_permitted = false;
    String TAG = "Permission";
    ImageView imageView;
    Uri uri_for_camera;
    FormValidator formValidator;
    private String userRole;
    private static final int REQUEST_ENABLE_BT = 1;
    String dti, qrText, bir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        addMenu = findViewById(R.id.addMenuButton);
        totalPriceMenu = findViewById(R.id.totalPriceLayout);
        imageView = findViewById(R.id.CapturedManual);
        Button addDefault = findViewById(R.id.addDefaultMenu);

        formValidator = new FormValidator();

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
        userRole = sharedPreferences.getString("userRole", "");


        // Initialize the dataAccess object with the Application context
        dataAccess = new DataAccess(getApplicationContext());

        dataAccess.getAllItems(Menu.this);

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();
        dataAccess.insertMovement(0, username, "Redirected to the Menu", updatedAt);


        // Set click listener for the "Add" button
        addMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the pop-up modal
                showAddMenuDialog();
            }
        });

        addDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataAccess.insertDefaultMenu(Menu.this);
                Intent intentInventory = new Intent(Menu.this, Menu.class);
                startActivity(intentInventory);
                Menu.this.overridePendingTransition(0, 0); // Remove transition animation
                Menu.this.finish();
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

        NavigationView adminNavigationView = findViewById(R.id.leftNavigationView);
        NavigationView staffNavigationView = findViewById(R.id.staffleftNavigationView);

        if (userRole.equals("Admin")) {
            adminNavigationView.setVisibility(View.VISIBLE);
            staffNavigationView.setVisibility(View.GONE);
            addMenu.setVisibility(View.VISIBLE);
            addDefault.setVisibility(View.VISIBLE);
        } else {
            adminNavigationView.setVisibility(View.GONE);
            staffNavigationView.setVisibility(View.VISIBLE);
            addMenu.setVisibility(View.GONE);
            addDefault.setVisibility(View.GONE);
        }


        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            // Handle the case accordingly
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
        } else if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled, prompt the user to enable it
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // Bluetooth is enabled, proceed with printing logic
            // Add your printing logic here
        }


        adminNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation item selection here
                int itemId = menuItem.getItemId();
                switch (itemId) {
                    case R.id.inventory:
                        // Start the new activity
                        Intent intentInventory = new Intent(Menu.this, Inventory.class);
                        startActivity(intentInventory);
                        Menu.this.overridePendingTransition(0, 0); // Remove transition animation
                        Menu.this.finish();

                        break;
                    case R.id.staff:
                        // Start the new activity
                        Intent intentStaff = new Intent(Menu.this, Staff.class);
                        startActivity(intentStaff);
                        Menu.this.overridePendingTransition(0, 0); // Remove transition animation
                        Menu.this.finish();

                        break;
                    case R.id.reports:
                        // Start the new activity
                        Intent intentReports = new Intent(Menu.this, Reports.class);
                        startActivity(intentReports);
                        Menu.this.overridePendingTransition(0, 0); // Remove transition animation
                        Menu.this.finish();

                        break;
                    case R.id.cash_drawer:
                        // Start the new activity
                        Intent intentCash = new Intent(Menu.this, CashDrawer.class);
                        startActivity(intentCash);
                        Menu.this.overridePendingTransition(0, 0); // Remove transition animation
                        Menu.this.finish();

                        break;
                    case R.id.transaction:
                        // Start the new activity
                        Intent intentTransactions = new Intent(Menu.this, Transactions.class);
                        startActivity(intentTransactions);
                        Menu.this.overridePendingTransition(0, 0); // Remove transition animation
                        Menu.this.finish();

                        break;
                    case R.id.settings:
                        // Start the new activity
                        Intent intentSettings = new Intent(Menu.this, Settings.class);
                        startActivity(intentSettings);
                        Menu.this.overridePendingTransition(0, 0); // Remove transition animation
                        Menu.this.finish();

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

        staffNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation item selection here
                int itemId = menuItem.getItemId();
                switch (itemId) {
                    case R.id.cash_drawer:
                        // Start the new activity
                        Intent intentCash = new Intent(Menu.this, CashDrawer.class);
                        startActivity(intentCash);
                        Menu.this.overridePendingTransition(0, 0); // Remove transition animation
                        Menu.this.finish();

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
        adapter.addFragment(new ShortOrdersFragment(), "SHORT ORDERS");
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

    @SuppressLint("Range")
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
        gCash.setText("GCash/Maya");
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

        String category = null;

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

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFormValid = formValidator.validateFormCheckout(tableLayout, orderType, paymentType);
                if (isFormValid) {
                    Toast.makeText(Menu.this, "" + final_paymentType, Toast.LENGTH_SHORT).show();
                    dataAccess.updatePendingOrder(orderNumber, final_orderType, final_paymentType, username);
                    if (final_paymentType.equals("GCash")) {
                        showGcashCheckOutDialog();
                    } else if (final_paymentType.equals("Cash")) {
                        showCheckOutDialog();
                    }
                    dialog.dismiss(); // Close the dialog after successful validation and processing
                }
            }
        });
        dialog.show();

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataAccess.clearOrders();
                totalPrice = 0;
                Intent intentMenu = new Intent(Menu.this, Menu.class);
                startActivity(intentMenu);
                Menu.this.overridePendingTransition(0, 0); // Remove transition animation
                Menu.this.finish();
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
        Button addMenu = dialogView.findViewById(R.id.addMenu);
        Button cancel = dialogView.findViewById(R.id.Cancel);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // Prevent dialog dismissal on touch outside
        dialog.setCancelable(false); // Prevent dialog cancellation on back press

        addMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFormValid = formValidator.validateFormAddMenu(menuNameEditText, menuCategorySpinner,
                        radioButtonHot, radioButtonCold, menuPriceEditText);
                if (isFormValid){
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
                        SharedPreferences sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

                        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                        String username = sharedPreferences.getString("username", "");
                        String userRole = sharedPreferences.getString("userRole", "");

                        // Get the current date and time
                        LocalDateTime currentDateTime = LocalDateTime.now();
                        String updatedAt = currentDateTime.toString();
                        dataAccess.insertMovement(0, username, "Added a new item to the menu " + menuName, updatedAt);
                        // Insertion was successful
                        Toast.makeText(Menu.this, "Menu added to the database", Toast.LENGTH_SHORT).show();
                        Intent intentMenu = new Intent(Menu.this, Menu.class);
                        startActivity(intentMenu);
                        Menu.this.overridePendingTransition(0, 0); // Remove transition animation
                        Menu.this.finish();
                        dialog.dismiss(); // Close the dialog
                    } else {
                        // Insertion failed
                        Toast.makeText(Menu.this, "Failed to add Menu to the database", Toast.LENGTH_SHORT).show();
                    }
                    // Dismiss the dialog only when the form is valid
                    dialog.dismiss();
                }

                Log.d("Validation","Validator: " + isFormValid);
            }
        });

        dialog.show();

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
                    radioButtonHot.setVisibility(View.VISIBLE);
                    radioButtonCold.setVisibility(View.VISIBLE);
                    radioButtonHot.setEnabled(true);
                    radioButtonCold.setEnabled(true);
                } else {
                    radioButtonHot.setVisibility(View.GONE);
                    radioButtonCold.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Close the dialog
            }
        });

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
                String paymentFinal = null;
                if (payment.getText().toString().trim().equals(null) && payment.getText().toString().trim().isEmpty()){
                    paymentFinal = "0";
                }else{
                    paymentFinal = payment.getText().toString().trim();
                }
                double totalPayment = Double.parseDouble(paymentFinal);
                if (totalPrice <= totalPayment) {
                    SharedPreferences sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

                    boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                    String username = sharedPreferences.getString("username", "");
                    String userRole = sharedPreferences.getString("userRole", "");

                    // Get the current date and time
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    String updatedAt = currentDateTime.toString();
                    dataAccess.insertMovement(0, username, "Check out an order in cash with the amount of " + totalPrice + " payment of " + totalPayment, updatedAt);
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




        confirm.setEnabled(false);
        confirm.setTextColor(getResources().getColor(R.color.light_gray));

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
                confirm.setEnabled(totalPrice <= totalPayment);
                if (totalPrice <= totalPayment){
                    confirm.setTextColor(getResources().getColor(R.color.primary));
                }
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
        Button manualCapture = dialogView.findViewById(R.id.manualCapture);

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
                    SharedPreferences sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

                    boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                    String username = sharedPreferences.getString("username", "");
                    String userRole = sharedPreferences.getString("userRole", "");

                    // Get the current date and time
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    String updatedAt = currentDateTime.toString();
                    dataAccess.insertMovement(0, username, "Check out an order in GCash/Maya with the amount of " + totalPrice + " payment of " + totalPayment, updatedAt);
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

        manualCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_camera_access_permitted){
                    openCamera();
                }else {
                    requestPermissionCameraAccess();
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
                                }else if (sender.equals("Maya") && message.startsWith("You have received")) {
                                    String receivedDate = sms.getTimestampMillis() + "";
                                    // Find the starting position of "from "
                                    int startIndex = message.indexOf("from ") + 5;
                                    // Find the ending position of " w/"
                                    int endIndex = message.indexOf(" with");
                                    // Extract the sender information
                                    String senderInfo = message.substring(startIndex, endIndex);
                                    // Split the sender information into name and number
                                    String[] parts = senderInfo.split("\\.");
                                    // Remove spaces and asterisks from the name
                                    String senderName = parts[0].trim();
                                    // Remove spaces from the number
                                    String senderNumber = parts[1].trim();

                                    int refNoIndex = message.indexOf("Ref. No:");
                                    String referenceNumber = message.substring(refNoIndex + 8).trim();

                                    int amountIndex = message.indexOf("PHP ");
                                    int amountEndIndex = message.indexOf(" on your");
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
    public void openCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Manual Cashless");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Captured using manual transaction");
        uri_for_camera = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri_for_camera);
        launcher_for_camera.launch(cameraIntent);

        String receipt_no = String.valueOf(dataAccess.getReceipt(orderNumber));
        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();
        String updatedAt = currentDateTime.toString();

        String filename = "Cashless_" + receipt_no + "_" + updatedAt;

    }

    private ActivityResultLauncher<Intent> launcher_for_camera =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK){
                                imageView.setImageURI(uri_for_camera);
                            }
                        }
                    }
            );

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

        ImageView qrCode = dialogView.findViewById(R.id.qrCode);
        TextView employeeName = dialogView.findViewById(R.id.employeeName);
        TextView transactionType = dialogView.findViewById(R.id.transactionType);
        View discountView = dialogView.findViewById(R.id.discountView);
        LinearLayout discountLayout = dialogView.findViewById(R.id.discountLayout);
        TextView discountType = dialogView.findViewById(R.id.discountType);
        TextView discountPrice = dialogView.findViewById(R.id.discountPrice);
        TextView paymentType = dialogView.findViewById(R.id.paymentType);
        TextView dtiNumber = dialogView.findViewById(R.id.dtiNumber);
        TextView datePrinted = dialogView.findViewById(R.id.datePrinted);
        TextView birNumber = dialogView.findViewById(R.id.birNumber);


        transactionNumberTextView.setText(""+orderNumber);

        tableLayout.removeAllViews();

        addTableHeader(tableLayout);

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
        String orderNote = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_NOTE));
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

        Cursor cursorDTI = dataAccess.getDTINumber();

        if (cursorDTI != null && cursorDTI.moveToFirst()) {
            do {
                // Retrieve the value from the cursor for the COLUMN_LAST_ORDER_NUMBER column
                @SuppressLint("Range") String lastDTI = cursorDTI.getString(cursorDTI.getColumnIndex(DatabaseHelper.COLUMN_LAST_DTI_NUMBER));

                // 2023030090866-07
                // Process the retrieved value as needed
                // For example, you can print it or assign it to a variable

                StringBuilder result = new StringBuilder();
                int count = 0;

                // Iterate through the input string from right to left
                for (int i = lastDTI.length() - 1; i >= 0; i--) {
                    char c = lastDTI.charAt(i);

                    // Check if the character is a digit (0-9)
                    if (Character.isDigit(c)) {
                        result.insert(0, c); // Add the digit to the result
                        count++; // Increase the count of digits found

                        // Break the loop when we have found five digits
                        if (count == 2) {
                            break;
                        }
                    }
                }

                String firstnumbers = lastDTI.replace(result,"");

                dti = firstnumbers + "-" + result;
                dtiNumber.setText(dti);
            } while (cursorDTI.moveToNext());
        }

        Cursor cursorQR = dataAccess.getQRCode();

        if (cursorQR != null && cursorQR.moveToFirst()) {
            do {
                // Retrieve the value from the cursor for the COLUMN_LAST_ORDER_NUMBER column
                @SuppressLint("Range") String lastQR = cursorQR.getString(cursorQR.getColumnIndex(DatabaseHelper.COLUMN_QR_TEXT));

                // Process the retrieved value as needed
                // For example, you can print it or assign it to a variable

                qrText = lastQR;
            } while (cursorQR.moveToNext());
        }

        Cursor cursorOR = dataAccess.getOR();

        if (cursorOR != null && cursorOR.moveToFirst()) {
            do {
                // Retrieve the value from the cursor for the COLUMN_LAST_ORDER_NUMBER column
                @SuppressLint("Range") String lastOR = cursorOR.getString(cursorOR.getColumnIndex(DatabaseHelper.COLUMN_OR_NUMBER));

                // Process the retrieved value as needed
                // For example, you can print it or assign it to a variable

                //birNum = String.valueOf(lastReceiptNumber);
                // Initialize variables to store the result
                StringBuilder result = new StringBuilder();
                int count = 0;

                // Iterate through the input string from right to left
                for (int i = lastOR.length() - 1; i >= 0; i--) {
                    char c = lastOR.charAt(i);

                    // Check if the character is a digit (0-9)
                    if (Character.isDigit(c)) {
                        result.insert(0, c); // Add the digit to the result
                        count++; // Increase the count of digits found

                        // Break the loop when we have found five digits
                        if (count == 5) {
                            break;
                        }
                    }
                }


                String firstnumbers = lastOR.replace(result,"");

                // Split the string into groups of three digits from the right (except the last group)
                List<String> groups = new ArrayList<>();
                int length = firstnumbers.length();
                for (int i = length; i > 0; i -= 3) {
                    int startIndex = Math.max(i - 3, 0);
                    groups.add(firstnumbers.substring(startIndex, i));
                }
                Collections.reverse(groups);

                // Join the groups with hyphens
                String resultFirst = TextUtils.join("-", groups);

                // Append '00000' at the end
                bir = resultFirst +"-"+ result;

                birNumber.setText(bir);
            } while (cursorOR.moveToNext());
        }

        discountLayout.setVisibility(View.GONE);
        discountView.setVisibility(View.GONE);
        discountPrice.setVisibility(View.GONE);
        if (!orderDiscountType.equals("No Discount")){
            discountLayout.setVisibility(View.VISIBLE);
            discountView.setVisibility(View.VISIBLE);
            discountPrice.setVisibility(View.VISIBLE);

            discountType.setText(orderDiscountType);
            discountPrice.setText(orderDiscount + "%");
        }

        LocalDateTime currentDateTime = LocalDateTime.now();
        String date_in = currentDateTime.toString();

        datePrinted.setText(date_in);

        String link = qrText;
        Bitmap qrCodeBitmap = generateQRCode(link, 500, 500);
        qrCode.setImageBitmap(qrCodeBitmap);

        employeeName.setText("Hi! My name is " + username);
        String capitalizedOrderType = orderType.substring(0, 1).toUpperCase() + orderType.substring(1);
        transactionType.setText(capitalizedOrderType);

        paymentType.setText(orderPaymentType);

        receiptNumberTextView.setText(receiptNumber);
        totalPriceTextView.setText("₱"+ orderTotal);
        totalPaymentTextView.setText("₱"+ orderPayment);
        totalChangeTextView.setText("₱"+ orderChange);

        Map<String, String> itemNotes = new HashMap<>();

        // Loop through the orderCursor to retrieve item names and notes
        while (orderCursor.moveToNext()) {
            String itemName = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_NAME));
            String itemNote = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_NOTE));
            String itemDiscountID = orderCursor.getString(orderCursor.getColumnIndex(DatabaseHelper.COLUMN_ORDER_DISCOUNT_TYPE));


            // Add the item note to the itemNotes map using the item name as the key
            itemNotes.put(itemName, itemNote);
        }

        // Now, use the itemNotes map to display the items and their corresponding notes
        int totalItems = itemQuantities.size(); // Get the total number of items
        int currentItem = 0; // Initialize the current item counter
        int itemNumber = 1;

        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            TableRow tableRow = new TableRow(this);

            TextView itemNameTextView = createTextViewTab(entry.getKey(), false);
            TextView quantityTextView = createTextViewTab(String.valueOf(orderQuantity), false);

            // Get the price for the current item
            double itemPrice = itemPrices.get(entry.getKey());
            Log.d("itemPriceMenu", String.valueOf(itemPrice) + orderQuantity);

            TextView priceTextView = createTextViewTab(String.valueOf(itemPrice), false);

            TableRow.LayoutParams cellLayoutParams = new TableRow.LayoutParams(
                    0,                                      // Set width to 0 to use weight
                    TableRow.LayoutParams.WRAP_CONTENT,     // Set height to WRAP_CONTENT
                    1f                                      // Set weight to 1 to take up available space
            );
            itemNameTextView.setLayoutParams(cellLayoutParams);
            quantityTextView.setLayoutParams(cellLayoutParams);
            priceTextView.setLayoutParams(cellLayoutParams);

            tableRow.addView(itemNameTextView);
            tableRow.addView(quantityTextView);
            tableRow.addView(priceTextView);

            // Adjust layout parameters to match header row weights
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            tableLayout.addView(tableRow);

            // Retrieve the note for the current item from the itemNotes map
            String itemNote = itemNotes.get(entry.getKey());

            // Add note row below the current row if there's a note for the item
            if (itemNote != null && !itemNote.isEmpty() ) {
                TableRow noteTableRow = new TableRow(this);
                TextView noteTextView = createTextViewTab("\t\t\t- " + itemNote, true);

                // Set text alignment to the left
                noteTextView.setGravity(Gravity.LEFT);

                // Set text size (change the value as needed)
                noteTextView.setTextSize(16); // Change the text size to 16sp (adjust as needed)

                TableRow.LayoutParams noteLayoutParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                noteLayoutParams.leftMargin = 0; // Remove the left margin
                noteTextView.setLayoutParams(noteLayoutParams);
                noteTableRow.addView(noteTextView);
                tableLayout.addView(noteTableRow);
            }

// Add the order discount type as a new row above the items
            if (orderDiscountType != null && !orderDiscountType.isEmpty() && !orderDiscountType.equals("No Discount")) {
                TableRow discountTypeTableRow = new TableRow(this);
                TextView discountTypeTextView = createTextViewTab("Discount Type: " + orderDiscountType, true);

                // Set text alignment to the left
                discountTypeTextView.setGravity(Gravity.LEFT);

                // Set text size (change the value as needed)
                discountTypeTextView.setTextSize(16); // Change the text size to 16sp (adjust as needed)

                TableRow.LayoutParams discountTypeLayoutParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                discountTypeLayoutParams.leftMargin = 0; // Remove the left margin
                discountTypeTextView.setLayoutParams(discountTypeLayoutParams);
                discountTypeTableRow.addView(discountTypeTextView);
                tableLayout.addView(discountTypeTableRow);
            }

            currentItem++;

            // Check if it's the last item, and add the note row if necessary
            if (currentItem == totalItems && orderNote != null && !orderNote.isEmpty()) {
                TableRow lastItemNoteRow = new TableRow(this);
                TextView lastItemNoteTextView = createTextViewTab("\t\t\t- " + orderNote, true);

                // Set text alignment to the left
                lastItemNoteTextView.setGravity(Gravity.LEFT);

                // Set text size (change the value as needed)
                lastItemNoteTextView.setTextSize(16); // Change the text size to 16sp (adjust as needed)

                TableRow.LayoutParams lastItemNoteLayoutParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                lastItemNoteLayoutParams.leftMargin = 0; // Remove the left margin
                lastItemNoteTextView.setLayoutParams(lastItemNoteLayoutParams);
                lastItemNoteRow.addView(lastItemNoteTextView);
                tableLayout.addView(lastItemNoteRow);
            }

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

                    LinearLayout linearLayout = (LinearLayout) dialogView;

// Create a bitmap of the linearLayout
                    linearLayout.setDrawingCacheEnabled(true);
                    linearLayout.measure(View.MeasureSpec.makeMeasureSpec(linearLayout.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    linearLayout.layout(0, 0, linearLayout.getMeasuredWidth(), linearLayout.getMeasuredHeight());

                    Bitmap bitmap = Bitmap.createBitmap(linearLayout.getWidth(), linearLayout.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    linearLayout.draw(canvas);

                    linearLayout.setDrawingCacheEnabled(false);

// Now you have the bitmap of the entire content of the LinearLayout


                    print.setVisibility(View.VISIBLE); // Show the print button again

                    int receipt_no = dataAccess.receiptNumber();
                    int order_no = dataAccess.orderNumber();

                    LocalDateTime currentDateTime = LocalDateTime.now();
                    String date_in = currentDateTime.toString();

                    String image_name = receipt_no + "-" + order_no + "-" + date_in;

                    String relativePath = Environment.DIRECTORY_DCIM + File.separator + "Receipt";

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, image_name + ".png");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);

                    Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    OutputStream outputStream = null;

                    try {
                        outputStream = getContentResolver().openOutputStream(imageUri);
                        if (outputStream != null) {
                            SharedPreferences sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE);

                            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                            String username = sharedPreferences.getString("username", "");
                            String userRole = sharedPreferences.getString("userRole", "");

                            // Get the current date and time
                            String updatedAt = currentDateTime.toString();
                            dataAccess.insertMovement(0, username, "Printed a receipt with a receipt # of " + receipt_no + " order # of " + order_no, updatedAt);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            outputStream.close();

                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                            BluetoothSocket socket = null;
                            OutputStream outputStreamPrint = null;

                            for (BluetoothDevice device : pairedDevices) {
                                if (device.getName().equals("Printer001")) {
                                    // Connect to the selected printer
                                    // You can use a BluetoothSocket to establish a connection
                                    UUID uuid = device.getUuids()[0].getUuid();
                                    try {
                                        socket = device.createRfcommSocketToServiceRecord(uuid);
                                        socket.connect();
                                        outputStreamPrint = socket.getOutputStream();
                                        break;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        // Handle connection error
                                    }
                                }
                            }

                            // Connect to the selected printer and establish the Bluetooth socket
                            if (socket != null && outputStreamPrint != null) {
                                View rootViewToPrint = dialogView.getRootView();
                                PrintTask printTask = new PrintTask(Menu.this, socket, outputStreamPrint, bitmap);
                                printTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }

                            Toast.makeText(Menu.this, "Receipt saved as image", Toast.LENGTH_SHORT).show();

                            Intent intentMenu = new Intent(Menu.this, Menu.class);
                            startActivity(intentMenu);
                            Menu.this.overridePendingTransition(0, 0); // Remove transition animation
                            Menu.this.finish();
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

    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(10, 20, 10, 20);
        textView.setTextSize(25);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT
        ));
        return textView;
    }

    private TextView createTextViewTab(String text, boolean isHeader) {
        float scale = getResources().getDisplayMetrics().density;
        int paddingInPixels = (int) (10 * scale + 0.5f); // 10dp converted to pixels

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(25); // Set the desired text size
        textView.setPadding(paddingInPixels, 5, paddingInPixels, 5); // Add padding (in pixels) to the TextView
        textView.setEllipsize(null); // Disable ellipsize
        textView.setMaxLines(Integer.MAX_VALUE); // Set maxLines to a high value for wrapping

        // Set the layout_width and layout_height attributes
        textView.setLayoutParams(new TableRow.LayoutParams(300, TableRow.LayoutParams.WRAP_CONTENT));

        if (isHeader) {
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        return textView;
    }

    private TextView createTextView(String text, boolean isHeader, int backgroundColor) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER); // Set gravity to center
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        return textView;
    }


    private void addTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        headerRow.setGravity(Gravity.CENTER); // Set the gravity to center

        TextView receiptNumberHeaderTextView = createHeaderTextView("Item(s)");
        receiptNumberHeaderTextView.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                1f
        ));
        headerRow.addView(receiptNumberHeaderTextView);

        TextView orderNumberHeaderTextView = createHeaderTextView("Quantity");
        orderNumberHeaderTextView.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                1f
        ));
        headerRow.addView(orderNumberHeaderTextView);

        TextView amountHeaderTextView = createHeaderTextView("Price");
        amountHeaderTextView.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                1f
        ));
        headerRow.addView(amountHeaderTextView);

        tableLayout.addView(headerRow);
    }






    private Bitmap generateQRCode(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            int bitMatrixWidth = bitMatrix.getWidth();
            int bitMatrixHeight = bitMatrix.getHeight();
            Bitmap qrCodeBitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.RGB_565);

            for (int x = 0; x < bitMatrixWidth; x++) {
                for (int y = 0; y < bitMatrixHeight; y++) {
                    qrCodeBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return qrCodeBitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
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

    public void requestPermissionStoragesImages(){
        if (ContextCompat.checkSelfPermission(Menu.this, required_permissions[0]) == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, required_permissions[0] + " Granted");
            is_storage_image_permitted = true;
            requestPermissionCameraAccess();
        } else {
            required_permissions_launcher_storage_images.launch(required_permissions[0]);
        }
    }

    private ActivityResultLauncher<String> required_permissions_launcher_storage_images =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted){
                            Log.d(TAG, required_permissions[0] + " Granted");
                            is_storage_image_permitted = true;
                        }else {
                            Log.d(TAG, required_permissions[0] + " Not Granted");
                            is_storage_image_permitted = false;
                        }
                        requestPermissionCameraAccess();
                    });
    public void requestPermissionCameraAccess(){
        if (ContextCompat.checkSelfPermission(Menu.this, required_permissions[1]) == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, required_permissions[1] + " Granted");
            is_camera_access_permitted = true;
        } else {
            required_permissions_launcher_camera_access.launch(required_permissions[1]);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth is enabled, proceed with printing logic
                // Add your printing logic here
            } else {
                // Bluetooth enable request was denied or failed
                // Handle the case accordingly
            }
        }
    }



    private ActivityResultLauncher<String> required_permissions_launcher_camera_access =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted){
                            Log.d(TAG, required_permissions[1] + " Granted");
                            is_camera_access_permitted = true;
                        }else {
                            Log.d(TAG, required_permissions[1] + " Not Granted");
                            is_camera_access_permitted = false;
                        }
                    });

}