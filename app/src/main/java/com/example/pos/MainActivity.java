package com.example.pos;

// Import necessary libraries
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    // Declare the views
    private EditText usernameEditText, passwordEditText;
    private Button loginButton;

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

        // Set click listener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the entered username and password
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Perform login validation by querying the database
                DataAccess dataAccess = new DataAccess(MainActivity.this);
                boolean loginSuccessful = dataAccess.loginUser(username, password);

                if (loginSuccessful) {
                    // Successful login, save the logged-in user's username in the session
                    dataAccess.saveLoggedInUser(MainActivity.this, username);

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



}
