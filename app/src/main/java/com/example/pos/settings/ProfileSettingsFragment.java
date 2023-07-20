package com.example.pos.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pos.DataAccess;
import com.example.pos.DatabaseHelper;
import com.example.pos.Menu;
import com.example.pos.R;
import com.example.pos.Settings;

public class ProfileSettingsFragment extends Fragment {

    String userName, name, password, conpassword;
    int userID;
    DataAccess dataAccess;

    public ProfileSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_settings, container, false);

        EditText Username = view.findViewById(R.id.usernameEditText);
        EditText Fullname = view.findViewById(R.id.nameEditText);
        EditText Password = view.findViewById(R.id.passwordEditText);
        EditText conPassword = view.findViewById(R.id.confirmEditText);
        Button buttonSave = view.findViewById(R.id.buttonSave);

        dataAccess = new DataAccess(requireContext());

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Session", Context.MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String loggedUsername = sharedPreferences.getString("username", "");
        String userRole = sharedPreferences.getString("userRole", "");

        // Retrieve user account data
        Cursor cursor = dataAccess.getUserAccount(loggedUsername);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String userIDs = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID));
            @SuppressLint("Range") String userName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME));
            @SuppressLint("Range") String fullName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FUll_NAME));
            // You can retrieve other data as well if needed

            // Set the retrieved data to the EditText fields
            Username.setHint(userName);
            Fullname.setHint(fullName);
            userID = Integer.parseInt(userIDs);
            // Set other data to corresponding EditText fields
        }


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = Username.getText().toString().trim();
                name = Fullname.getText().toString().trim();
                password = Password.getText().toString().trim();
                conpassword = conPassword.getText().toString().trim();

                if (userName.trim().isEmpty() && name.trim().isEmpty() && password.trim().isEmpty() && conpassword.trim().isEmpty()) {
                    // No fields to update, show an error message
                    Toast.makeText(getContext(), "No fields to update", Toast.LENGTH_SHORT).show();
                } else {
                    if (!password.equals(null)) {
                        if (password.equals(conpassword)) {
                            int updatedUserID = dataAccess.updateUser(userName, loggedUsername, name, password, conpassword, userID, getContext());
                            if (updatedUserID != -1) {
                                // Update success
                                Toast.makeText(getContext(), "Update Success", Toast.LENGTH_SHORT).show();

                                // Perform additional actions
                                dataAccess.logoutUser(getContext());
                                getActivity().finish();
                            } else {
                                // Duplicate username error
                                Toast.makeText(getContext(), "Username is already taken", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Password does not match
                            Toast.makeText(getContext(), "Password does not match!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        int updatedUserID = dataAccess.updateUser(userName, loggedUsername, name, password, conpassword, userID, getContext());
                        if (updatedUserID != -1) {
                            // Update success
                            Toast.makeText(getContext(), "Update Success", Toast.LENGTH_SHORT).show();

                            // Perform additional actions
                            dataAccess.logoutUser(getContext());
                            getActivity().finish();
                        } else {
                            // Duplicate username error
                            Toast.makeText(getContext(), "Username is already taken", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });


        return view;
    }
}