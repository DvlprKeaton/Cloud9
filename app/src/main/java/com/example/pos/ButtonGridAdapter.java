package com.example.pos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

public class ButtonGridAdapter extends BaseAdapter {
    private Context context;
    private String[] buttonNames;

    public ButtonGridAdapter(Context context, String[] buttonNames) {
        this.context = context;
        this.buttonNames = buttonNames;
    }

    @Override
    public int getCount() {
        return buttonNames.length;
    }

    @Override
    public Object getItem(int position) {
        return buttonNames[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_button, parent, false);
        }

        Button button = convertView.findViewById(R.id.gridItemButton);
        button.setText(buttonNames[position]);

        return convertView;
    }
}
