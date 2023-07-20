package com.example.pos;

// Import necessary libraries
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Declare the views
    private EditText usernameEditText, passwordEditText;
    private Button loginButton;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the views
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        DataAccess dataAccess = new DataAccess(MainActivity.this);
        boolean isLoggedIn = dataAccess.getLoggedInUser(MainActivity.this);

        if (isLoggedIn) {
            // User is already logged in, proceed to the next screen or perform necessary actions
            Intent intent = new Intent(MainActivity.this, Menu.class);
            startActivity(intent);
            finish();
        }

        // Step 1: Check if permissions are granted
        if (checkPermissions()) {
            // Permissions are already granted, proceed with your logic
            // ...
        } else {
            // Step 2: Request permissions
            requestPermissions();
        }

        // Set click listener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the entered username and password
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Perform login validation by querying the database
                DataAccess dataAccess = new DataAccess(MainActivity.this);
                Map<String, String> loginDetails = dataAccess.loginUser(username, password);

                boolean loginSuccessful = Boolean.parseBoolean(loginDetails.get("loginSuccessful"));
                if (loginSuccessful) {
                    String userRole = String.valueOf(loginDetails.get("userRole"));
                    // Save the logged-in user's username and role in the session
                    dataAccess.saveLoggedInUser(MainActivity.this, username, userRole);

                    // Show a toast message
                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    // TODO: Proceed to the next screen or perform necessary actions

                    Intent intent = new Intent(MainActivity.this, Menu.class);
                    startActivity(intent);

                    // Finish the current activity
                    finish();
                } else {
                    // Invalid login, show a toast message
                    Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Step 3: Check for permissions
    private boolean checkPermissions() {
        int smsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return smsPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean smsPermissionRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS);
        boolean storagePermissionRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        Log.d("Permissions", smsPermissionRationale + " - " + storagePermissionRationale);

        if (smsPermissionRationale || storagePermissionRationale) {
            // Show a dialog or message explaining why the permissions are needed
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Request");
            builder.setMessage("This app requires SMS permission to process GCash transactions and external storage permission to save receipts. Please grant the permissions to continue.");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Request permissions again
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
            });

            builder.show();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if the required permissions are granted after returning from the app settings
            if (checkPermissions()) {
                // Permissions granted, proceed with your logic
                // ...
            } else {
                // Permissions not granted, handle accordingly (e.g., show a message and close the app)
                Toast.makeText(this, "Permissions not granted. The app will now close.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
