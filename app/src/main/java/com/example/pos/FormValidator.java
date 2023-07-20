package com.example.pos;

import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;

public class FormValidator {

    public static boolean validateFormAddMenu(EditText menuNameEditText, Spinner menuCategorySpinner,
                                       RadioButton radioButtonHot, RadioButton radioButtonCold,
                                       EditText menuPriceEditText) {

        // Validate Menu Name
        String menuName = menuNameEditText.getText().toString().trim();
        if (menuName.isEmpty()) {
            menuNameEditText.setError("Product Name is required");
            return false;
        }

        // Validate Menu Category
        String menuCategory = menuCategorySpinner.getSelectedItem().toString();
        if (menuCategory.isEmpty()) {
            // Show error message or handle validation as needed
            return false;
        }

        if (menuCategory.equals("Espresso") || menuCategory.equals("Non Espresso")) {
            if (!radioButtonHot.isChecked() && !radioButtonCold.isChecked()) {
                Toast.makeText(menuNameEditText.getContext(), "Please select a temperature", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Validate Menu Price
        String menuPrice = menuPriceEditText.getText().toString().trim();
        if (menuPrice.isEmpty()) {
            menuPriceEditText.setError("Price is required");
            return false;
        }

        // Additional validation logic for price format, category selection, etc.
        // Add your own validation rules based on your requirements

        return true;
    }

    public boolean validateFormAddOrder(Spinner menuCategorySpinner, RadioGroup radioGroupTemperature, RadioGroup discountGroup
            , RadioGroup couponGroup, LinearLayout discountLayout, LinearLayout couponLayout, LinearLayout tempLayout) {


        // Validate temperature selection if visible
        if (tempLayout.getVisibility() == View.VISIBLE) {
            int selectedTemperatureId = radioGroupTemperature.getCheckedRadioButtonId();
            if (selectedTemperatureId == -1) {
                Toast.makeText(menuCategorySpinner.getContext(), "Please select a temperature", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Validate discount selection if visible
        if (discountLayout.getVisibility() == View.VISIBLE) {
            int selectedDiscountId = discountGroup.getCheckedRadioButtonId();
            if (selectedDiscountId == -1) {
                Toast.makeText(menuCategorySpinner.getContext(), "Please select a discount", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Validate coupon selection if visible
        if (couponLayout.getVisibility() == View.VISIBLE) {
            int selectedCouponId = couponGroup.getCheckedRadioButtonId();
            if (selectedCouponId == -1) {
                Toast.makeText(menuCategorySpinner.getContext(), "Please select a coupon", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Additional validation logic

        return true;
    }

    public boolean validateFormCheckout(TableLayout tableLayout, RadioGroup radioGroupOrderType, RadioGroup radioGroupPaymentType) {
        // Validate if any items are added
        if (tableLayout.getChildCount() <= 1) {
            Toast.makeText(tableLayout.getContext(), "Please add items to the order", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate order type selection
        int selectedOrderTypeId = radioGroupOrderType.getCheckedRadioButtonId();
        if (selectedOrderTypeId == -1) {
            Toast.makeText(radioGroupOrderType.getContext(), "Please select an order type", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate payment type selection
        int selectedPaymentTypeId = radioGroupPaymentType.getCheckedRadioButtonId();
        if (selectedPaymentTypeId == -1) {
            Toast.makeText(radioGroupPaymentType.getContext(), "Please select a payment type", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Additional validation logic

        return true;
    }

}