package com.example.pos.menu;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pos.ButtonGridAdapter;
import com.example.pos.ButtonTileAdapter;
import com.example.pos.DataAccess;
import com.example.pos.R;

public class EspressoFragment extends Fragment {

    private GridLayout gridLayout;
    private ButtonTileAdapter buttonTileAdapter;

    private String[] buttonNames = {
            "Americano",
            "Cappuccino",
            "Cafe Latte",
            "Mocha Latte",
            "White Chocolate",
            "Caramel Macchiato",
            "Salted Caramel",
            "Spanish Latte",
            "Hazelnut Latte",
            "Dirty Matcha",
            "Oreo Cream Latte",
            "SMAK Latte",
            // Add more button names as needed
    };

    private double[] buttonPrices = {
            120.00,
            140.00,
            150.00,
            160.00,
            160.00,
            160.00,
            160.00,
            170.00,
            170.00,
            180.00,
            180.00,
            190.00,
            // Add more button prices in the same order as buttonNames
    };

    private TextView totalPriceValue;
    private double totalPrice = 0.0;
    private Button clearButton;
    private Button checkoutButton;

    public EspressoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_espresso, container, false);

        gridLayout = view.findViewById(R.id.gridLayout);
        buttonTileAdapter = new ButtonTileAdapter(buttonNames);
        addButtonTiles();

        totalPriceValue = requireActivity().findViewById(R.id.totalPriceValue);

        clearButton = requireActivity().findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalPrice = 0.0;
                totalPriceValue.setText(String.format("%.2f", totalPrice));
                // Add code to clear any other data or perform actions when Clear Button is clicked
            }
        });

        checkoutButton = requireActivity().findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    private void addButtonTiles() {
        for (int i = 0; i < buttonNames.length; i++) {
            View tileView = LayoutInflater.from(requireContext()).inflate(R.layout.grid_item_button, gridLayout, false);
            CardView cardView = tileView.findViewById(R.id.cardView);
            Button button = tileView.findViewById(R.id.gridItemButton);
            button.setText(buttonNames[i]);
            final double price = buttonPrices[i];
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    totalPrice += price;
                    totalPriceValue.setText(String.format("%.2f", totalPrice));

                   /* DataAccess dataAccess = new DataAccess(getContext());

                    // Insert a transaction
                    long transactionId = dataAccess.insertTransaction(120.00);
                    if (transactionId != -1) {
                        // Insertion successful
                    } else {
                        // Insertion failed
                    }

                    // Insert a user
                    long userId = dataAccess.insertUser("John Doe");
                    if (userId != -1) {
                        // Insertion successful
                    } else {
                        // Insertion failed
                    }*/

                }
            });
            gridLayout.addView(cardView);
        }
    }
}