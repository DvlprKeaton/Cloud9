package com.example.pos.menu;

import static android.view.View.GONE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pos.ButtonData;
import com.example.pos.ButtonTileAdapter;
import com.example.pos.DataAccess;
import com.example.pos.DatabaseHelper;
import com.example.pos.FormValidator;
import com.example.pos.LoadingScreenDialog;
import com.example.pos.Menu;
import com.example.pos.R;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EspressoFragment extends Fragment {

    private GridLayout gridLayout;
    private ButtonTileAdapter buttonTileAdapter;

    private TextView totalPriceValue;
    private double totalPrice = 0.0;
    private Button clearButton;
    private Button checkoutButton;
    private DataAccess dataAccess;
    private int maxQuantity = 1;
    private DatabaseHelper dbHelper;
    private String username;
    private int orderNumber;
    private LoadingScreenDialog loadingScreenDialog;
    FormValidator formValidator;
    String productData = "Espresso";
    public EspressoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_espresso, container, false);

        gridLayout = view.findViewById(R.id.gridLayout);
        totalPriceValue = requireActivity().findViewById(R.id.totalPriceValue);

        dataAccess = new DataAccess(getActivity());

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("Session", Context.MODE_PRIVATE);

        // Create an instance of DatabaseHelper in your Application class
        dbHelper = new DatabaseHelper(getContext());
        formValidator = new FormValidator();

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        username = sharedPreferences.getString("username", "");
        String userRole = sharedPreferences.getString("userRole", "");


        orderNumber = dataAccess.orderNumber();
        totalPrice = dataAccess.getPendingTotal(String.valueOf(orderNumber));
        totalPriceValue.setText(String.format("%.2f", totalPrice));

        loadingScreenDialog = new LoadingScreenDialog(requireContext());

        addButtonTiles();

        return view;
    }

    private void addButtonTiles() {
        List<ButtonData> buttons = dataAccess.getProductData(productData);

        String[] buttonNames = new String[buttons.size()];
        double[] buttonPrices = new double[buttons.size()];

        for (int i = 0; i < buttons.size(); i++) {
            ButtonData button = buttons.get(i);
            buttonNames[i] = button.getName();
            buttonPrices[i] = button.getPrice();
        }

        buttonTileAdapter = new ButtonTileAdapter(buttonNames);

        for (int i = 0; i < buttons.size(); i++) {
            View tileView = LayoutInflater.from(requireContext()).inflate(R.layout.grid_item_button, gridLayout, false);
            CardView cardView = tileView.findViewById(R.id.cardView);
            Button buttonView = tileView.findViewById(R.id.gridItemButton);
            buttonView.setText(buttonNames[i]);
            final String name = buttonNames[i];
            final double price = buttonPrices[i];
            buttonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    showOrderItem(String.valueOf(name), price);

                }
            });
            gridLayout.addView(cardView);
        }
    }

    private void showOrderItem(String name, double price) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Take Order");

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_order_dialog, null);
        builder.setView(dialogView);

        // Find views in the custom layout
        TextView productName = dialogView.findViewById(R.id.itemName);
        RadioButton radioButtonHot = dialogView.findViewById(R.id.radioButtonHot);
        RadioButton radioButtonCold = dialogView.findViewById(R.id.radioButtonCold);
        EditText quantityTextView = dialogView.findViewById(R.id.quantityTextView);
        Button decreaseButton = dialogView.findViewById(R.id.decreaseButton);
        Button increaseButton = dialogView.findViewById(R.id.increaseButton);
        Spinner orderCategorySpinner = dialogView.findViewById(R.id.orderCategorySpinner);
        LinearLayout discount = dialogView.findViewById(R.id.dicountID);
        LinearLayout coupon = dialogView.findViewById(R.id.coupon);
        LinearLayout tempLayout = dialogView.findViewById(R.id.TempLayout);
        RadioButton radioSenior = dialogView.findViewById(R.id.seniorID);
        RadioButton radioPWD = dialogView.findViewById(R.id.pwdID);
        RadioButton radioFiveStamps = dialogView.findViewById(R.id.fiveStamps);
        RadioButton radioTenStamps = dialogView.findViewById(R.id.tenStamps);
        Button addOrder = dialogView.findViewById(R.id.addOrder);
        Button cancel = dialogView.findViewById(R.id.Cancel);
        RadioGroup tempGroup = dialogView.findViewById(R.id.radioGroupTemperature);
        RadioGroup discountGroup = dialogView.findViewById(R.id.discountGroup);
        RadioGroup couponGroup = dialogView.findViewById(R.id.couponGroup);

        EditText notes = dialogView.findViewById(R.id.notesEditText);

        // Set initial quantity
        final int[] quantity = {1};
        quantityTextView.setText(String.valueOf(quantity[0]));

        // Set Product name
        productName.setText(name);

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
                if (quantity[0] < maxQuantity) {
                    quantity[0]++;
                    quantityTextView.setText(String.valueOf(quantity[0]));
                } else {
                    Toast.makeText(getContext(), "Maximum quantity reached", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // Prevent dialog dismissal on touch outside
        dialog.setCancelable(false); // Prevent dialog cancellation on back press

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("Session", Context.MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", "");
        String userRole = sharedPreferences.getString("userRole", "");

        Button deleteButton = dialogView.findViewById(R.id.deleteMenu);

        deleteButton.setVisibility(GONE);
        if (userRole.equals("Admin")){
            deleteButton.setVisibility(View.VISIBLE);
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataAccess.deleteMenu(name, getContext());
                Intent intent = new Intent(getActivity(), Menu.class);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0); // Remove transition animation
                getActivity().finish();
                dialog.dismiss();
            }
        });

        addOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFormValid = formValidator.validateFormAddOrder(orderCategorySpinner,tempGroup, discountGroup, couponGroup, discount, coupon, tempLayout);
                String orderType = "Espresso";
                boolean disc = dataAccess.getDiscountLimit(orderNumber, orderType);
                if (disc){
                    Toast.makeText(getContext(), "You have exceeded the limit for discount", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                if (isFormValid){
                        EditText menuCategory = quantityTextView;
                        String discount = orderCategorySpinner.getSelectedItem().toString();
                        double discountPercentage = 0;
                        double productPrice = price;
                        boolean isHot = radioButtonHot.isChecked();
                        boolean senior = radioSenior.isChecked();
                        boolean pwd = radioPWD.isChecked();
                        boolean fiveStamps = radioFiveStamps.isChecked();
                        boolean tenStamps = radioTenStamps.isChecked();
                        String discountValue = null;
                        String orderNote = notes.getText().toString().trim();


                        if (discount.equals("Discount ID") && senior || pwd) {
                            discountPercentage = 10;
                            if (senior) {
                                discountValue = "Senior ID";
                            } else if (pwd) {
                                discountValue = "PWD ID";
                            }
                        } else if (discount.equals("Coupon") && fiveStamps) {
                            discountPercentage = 10;
                            discountValue = "Five Stamps";

                        } else if (discount.equals("Coupon") && tenStamps) {
                            discountPercentage = 100;
                            discountValue = "Ten Stamps";
                        } else if (discount.equals("No Discount")) {
                            discountPercentage = 0;
                            discountValue = discount;
                        }

                        // Get other order details
                        int quantity = Integer.parseInt(quantityTextView.getText().toString());
                        double discountAmount = discountPercentage; // Calculate the discount amount based on the discount type and other factors
                        String discountType = discount; // Determine the discount type based on your logic
                        //String paymentType = "Cash"; // Get the selected payment type
                        //double payment = 0.0; // Get the payment amount
                        double total = calculateTotal(quantity, productPrice, discountAmount); // Calculate the total amount
                        //double change = calculateChange(payment, total); // Calculate the change amount
                        String addedBy = username; // Get the user's name or identifier



                        // Insert the pending order into the database
                        long newRowId = dataAccess.insertPendingOrders(name, quantity, orderType, discountAmount, discountValue, null, 0.0, total, 0.0, orderNote, addedBy);

                        if (newRowId != -1) {
                            showLoadingScreen();
                            Intent intent = new Intent(getActivity(), Menu.class);
                            startActivity(intent);
                            getActivity().overridePendingTransition(0, 0); // Remove transition animation
                            getActivity().finish();
                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("Session", Context.MODE_PRIVATE);

                            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                            String username = sharedPreferences.getString("username", "");
                            String userRole = sharedPreferences.getString("userRole", "");

                            // Get the current date and time
                            LocalDateTime currentDateTime = LocalDateTime.now();
                            String updatedAt = currentDateTime.toString();
                            dataAccess.insertMovement(0, username, "Added an order of Espresso " + name, updatedAt);
                            Toast.makeText(getContext(), "Order placed successfully", Toast.LENGTH_SHORT).show();
                            hideLoadingScreen();
                        } else {
                            // Insertion failed
                            Toast.makeText(getContext(), "Failed to place order", Toast.LENGTH_SHORT).show();
                            hideLoadingScreen();
                        }

                    dialog.dismiss(); // Close the dialog
                }
            }
        });

        dialog.show();

        List<String> menuCategories = getMenuCategories();
        ArrayAdapter<String> menuCategoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, menuCategories);
        orderCategorySpinner.setAdapter(menuCategoryAdapter);

        // Set the initial state of radio buttons
        coupon.setVisibility(GONE);
        discount.setVisibility(GONE);
        tempLayout.setVisibility(GONE);

        if (productData.equals("Espresso") || productData.equals("Non Espresso")){
            tempLayout.setVisibility(View.VISIBLE);
        }

        orderCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = menuCategories.get(position);
                if (selectedCategory.equals("Discount ID") || selectedCategory.equals("Coupon")) {
                    maxQuantity = 1;
                } else {
                    maxQuantity = Integer.MAX_VALUE; // No maximum quantity
                }

                // Update the quantity if it exceeds the new maximum
                if (quantity[0] > maxQuantity) {
                    quantity[0] = maxQuantity;
                    quantityTextView.setText(String.valueOf(quantity[0]));
                }

                // Update the visibility of discount and coupon layouts
                if (selectedCategory.equals("Discount ID")) {
                    discount.setVisibility(View.VISIBLE);
                    coupon.setVisibility(GONE);
                    radioFiveStamps.setChecked(false);
                    radioTenStamps.setChecked(false);
                } else if(selectedCategory.equals("Coupon")) {
                    coupon.setVisibility(View.VISIBLE);
                    discount.setVisibility(GONE);
                    radioPWD.setChecked(false);
                    radioSenior.setChecked(false);
                } else {
                    coupon.setVisibility(GONE);
                    discount.setVisibility(GONE);
                    radioPWD.setChecked(false);
                    radioSenior.setChecked(false);
                    radioFiveStamps.setChecked(false);
                    radioTenStamps.setChecked(false);
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

    private List<String> getMenuCategories() {
        List<String> menuCategories = new ArrayList<>();
        menuCategories.add("No Discount");
        menuCategories.add("Discount ID");
        menuCategories.add("Coupon");
        return menuCategories;
    }

    private double calculateTotal(int quantity, double price, double discount) {
        double subtotal = 1 * price;
        double total = subtotal - (subtotal * (discount / 100));
        return total;
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
    public void onDestroy() {
        super.onDestroy();

        // Make sure to dismiss the loading screen dialog when the activity is destroyed
        hideLoadingScreen();
    }



}
