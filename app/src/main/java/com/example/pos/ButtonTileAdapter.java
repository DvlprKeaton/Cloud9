package com.example.pos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ButtonTileAdapter extends RecyclerView.Adapter<ButtonTileAdapter.ButtonTileViewHolder> {
    private String[] buttonNames;

    public ButtonTileAdapter(String[] buttonNames) {
        this.buttonNames = buttonNames;
    }

    @Override
    public ButtonTileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_button, parent, false);
        return new ButtonTileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ButtonTileViewHolder holder, int position) {
        String buttonName = buttonNames[position];
        holder.bindButtonTile(buttonName);
    }

    @Override
    public int getItemCount() {
        return buttonNames.length;
    }

    public class ButtonTileViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private Button button;

        public ButtonTileViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            button = itemView.findViewById(R.id.gridItemButton);
        }

        public void bindButtonTile(String buttonName) {
            button.setText(buttonName);
        }
    }
}
