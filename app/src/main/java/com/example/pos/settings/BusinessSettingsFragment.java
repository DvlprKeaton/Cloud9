package com.example.pos.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pos.DataAccess;
import com.example.pos.DatabaseHelper;
import com.example.pos.Inventory;
import com.example.pos.Menu;
import com.example.pos.R;
import com.example.pos.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BusinessSettingsFragment extends Fragment {

    String birNum, dtiNum, orderNum, qrCode, receiptNum;
    DataAccess dataAccess;

    public BusinessSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_business_settings, container, false);

        EditText bir = view.findViewById(R.id.birEditText);
        EditText receipt = view.findViewById(R.id.receiptEditText);
        EditText dti = view.findViewById(R.id.dtiEditText);
        EditText order = view.findViewById(R.id.orderEditText);
        EditText qr = view.findViewById(R.id.qrEditText);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        dataAccess = new DataAccess(requireContext());

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Session", Context.MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String loggedUsername = sharedPreferences.getString("username", "");
        String userRole = sharedPreferences.getString("userRole", "");

        Cursor cursorOrder = dataAccess.getOrderNumber();

        if (cursorOrder != null && cursorOrder.moveToFirst()) {
            do {
                // Retrieve the value from the cursor for the COLUMN_LAST_ORDER_NUMBER column
                @SuppressLint("Range") String lastOrderNumber = cursorOrder.getString(cursorOrder.getColumnIndex(DatabaseHelper.COLUMN_LAST_ORDER_NUMBER));

                orderNum = String.valueOf(lastOrderNumber);
                order.setHint("Order #: " + orderNum);
            } while (cursorOrder.moveToNext());
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
                birNum = resultFirst +"-"+ result;

                bir.setHint("BIR #: " + birNum);
            } while (cursorOR.moveToNext());
        }

        Cursor cursorReceipt = dataAccess.getReceiptNumber();

        if (cursorReceipt != null && cursorReceipt.moveToFirst()) {
            do {
                // Retrieve the value from the cursor for the COLUMN_LAST_ORDER_NUMBER column
                @SuppressLint("Range") String lastReceiptNumber = cursorReceipt.getString(cursorReceipt.getColumnIndex(DatabaseHelper.COLUMN_LAST_RECEIPT_NUMBER));

                orderNum = String.valueOf(lastReceiptNumber);

                receipt.setHint("Receipt #: " + orderNum);
            } while (cursorReceipt.moveToNext());
        }

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

                dtiNum = firstnumbers + "-" + result;

                dti.setHint("DTI #: " + dtiNum);
            } while (cursorDTI.moveToNext());
        }

        Cursor cursorQR = dataAccess.getQRCode();

        if (cursorQR != null && cursorQR.moveToFirst()) {
            do {
                // Retrieve the value from the cursor for the COLUMN_LAST_ORDER_NUMBER column
                 @SuppressLint("Range") String lastQR = cursorQR.getString(cursorQR.getColumnIndex(DatabaseHelper.COLUMN_QR_TEXT));

                // Process the retrieved value as needed
                // For example, you can print it or assign it to a variable

                qrCode = lastQR;
                qr.setHint("QRCode Value: " + qrCode);
            } while (cursorQR.moveToNext());
        }

        Log.d("Business: ", "DTI" + dtiNum + ", BIR" + birNum);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputOrder = order.getText().toString().trim();
                String inputBIR = bir.getText().toString().trim();
                String inputDTI = dti.getText().toString().trim();
                String inputQR= qr.getText().toString().trim();
                String inputReceipt= receipt.getText().toString().trim();

                Log.d("Saving Business: ", inputOrder + inputBIR + inputDTI+ inputQR +inputReceipt);

                if (birNum.trim().isEmpty() && orderNum.trim().isEmpty() && dtiNum.trim().isEmpty() && qrCode.trim().isEmpty() && inputReceipt.trim().isEmpty()) {
                    // No fields to update, show an error message
                    Toast.makeText(getContext(), "No fields to update", Toast.LENGTH_SHORT).show();
                } else{
                    int updateBusiness = dataAccess.updateBusiness(inputBIR, inputDTI, inputOrder, inputQR, inputReceipt, getContext());
                    if (updateBusiness != 0) {
                        // Update success
                        Toast.makeText(getContext(), "Update Success", Toast.LENGTH_SHORT).show();

                        Intent intentInventory = new Intent(getContext(), Settings.class);
                        startActivity(intentInventory);
                        getActivity().overridePendingTransition(0, 0); // Remove transition animation
                        getActivity().finish();
                    } else {
                        // Duplicate username error
                        Toast.makeText(getContext(), "Nothing was changed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        return view;
    }
}