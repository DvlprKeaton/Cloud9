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

import com.example.pos.ButtonGridAdapter;
import com.example.pos.ButtonTileAdapter;
import com.example.pos.R;

public class FruitTeaFragment extends Fragment {

    private GridLayout gridLayout;
    private ButtonTileAdapter buttonTileAdapter;

    private String[] buttonNames = {
            "Strawberry",
            "Passion Fruit",
            "Green Apple",
            "Lychee",
            "Mango"
            // Add more button names as needed
    };

    public FruitTeaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fruit_tea, container, false);

        gridLayout = view.findViewById(R.id.gridLayout);
        buttonTileAdapter = new ButtonTileAdapter(buttonNames);
        addButtonTiles();

        return view;
    }

    private void addButtonTiles() {
        for (int i = 0; i < buttonNames.length; i++) {
            View tileView = LayoutInflater.from(requireContext()).inflate(R.layout.grid_item_button, gridLayout, false);
            CardView cardView = tileView.findViewById(R.id.cardView);
            Button button = tileView.findViewById(R.id.gridItemButton);
            button.setText(buttonNames[i]);
            gridLayout.addView(cardView);
        }
    }
}