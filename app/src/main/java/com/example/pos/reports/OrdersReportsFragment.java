package com.example.pos.reports;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.pos.DataAccess;
import com.example.pos.DatePickerDialogFragment;
import com.example.pos.ExcelExporter;
import com.example.pos.R;
import com.example.pos.Reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrdersReportsFragment extends Fragment implements DatePickerDialogFragment.OnDateSelectedListener {

    private FrameLayout pieChartContainer;
    private DataAccess dataAccess;
    private LinearLayout legendContainer;
    private Spinner spinner_category;
    String catergorySelected ="overall";
    String startDate, endDate, dateSelected;
    Button startDate_btn, endDate_btn;
    EditText startDate_ET, endDate_ET;

    public OrdersReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders_reports, container, false);

        pieChartContainer = view.findViewById(R.id.pieChartContainer);
        legendContainer = view.findViewById(R.id.legendContainer);

        final ViewGroup parentView = (ViewGroup) pieChartContainer.getParent();

        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Retrieve the width and height of the pieChartContainer
                int width = pieChartContainer.getWidth();
                int height = pieChartContainer.getHeight();

                // Remove the listener to avoid redundant calls
                parentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                pieChartContainer.removeAllViews(); // Clear previous pie chart views
                legendContainer.removeAllViews(); // Clear previous legend views

                // Proceed with drawing the pie chart using the obtained dimensions
                drawPieChart(600, 600, catergorySelected, dateSelected);

                Log.d("PieChartGlobal", "Width: " + width + ", Height: " + height);
            }
        });

        dataAccess = new DataAccess(getContext());

        spinner_category = view.findViewById(R.id.spinner_category);
        startDate_btn = view.findViewById(R.id.button_start_date);
        endDate_btn = view.findViewById(R.id.button_end_date);
        startDate_ET = view.findViewById(R.id.editText_start_date);
        endDate_ET = view.findViewById(R.id.editText_end_date);

        startDate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(true);
            }
        });

        endDate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(false);
            }
        });


        // Create an ArrayAdapter using the custom layout and the menu categories list
        @SuppressLint("ResourceType") ArrayAdapter<String> adapter_category = new ArrayAdapter<String>(
                getContext(),
                R.layout.spinner_item,
                getMenuCategories());

        // Set the adapter for the spinner
        spinner_category.setAdapter(adapter_category);

        spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = (String) parent.getItemAtPosition(position);

                catergorySelected = selectedCategory;
                dataAccess.getOrdersPieChartDaily(catergorySelected, startDate, endDate);
                updatePieChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case when nothing is selected
            }
        });


        Button exportExcel = view.findViewById(R.id.exportExcel_btn);
        exportExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Integer> data = dataAccess.getOrdersPieChartDaily(catergorySelected, startDate, endDate);

                // Create a list to hold the formatted data for exporting
                List<String> formattedData = new ArrayList<>();

                // Iterate over the map and format the data as required
                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    String itemName = entry.getKey();
                    int count = entry.getValue();
                    String formattedEntry = itemName + ": " + count;
                    formattedData.add(formattedEntry);
                }

                // Pass the formatted data list and the current context to the exportToExcel method
                ExcelExporter.exportToExcel(getContext(), formattedData,null, "OrdersReport " + catergorySelected, startDate,endDate);
                updatePieChart();
            }
        });



        return view;
    }

    private void updatePieChart() {
        pieChartContainer.removeAllViews(); // Clear previous pie chart views
        legendContainer.removeAllViews(); // Clear previous legend views

        // Proceed with drawing the pie chart using the obtained dimensions
        drawPieChart(600, 600, catergorySelected, dateSelected);
    }

    private void showDatePickerDialog(boolean isStartDate) {
        DatePickerDialogFragment datePickerDialogFragment = DatePickerDialogFragment.newInstance();
        datePickerDialogFragment.setOnDateSelectedListener(new DatePickerDialogFragment.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day) {
                String formattedDate = formatDate(year, month, day);

                if (isStartDate) {
                    startDate_ET.setText(formattedDate);
                    startDate = formattedDate;
                } else {
                    endDate_ET.setText(formattedDate);
                    endDate = formattedDate;
                }
                updatePieChart();
            }
        });
        datePickerDialogFragment.show(getParentFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSelected(int year, int month, int day) {
        // Handle the selected date
        // You can update the selected date in your UI or perform any other necessary actions
        // For example, you can update the "dateSelected" variable and call the updatePieChart() method
        dateSelected = formatDate(year, month, day);
        updatePieChart();
    }

    private String formatDate(int year, int month, int day) {
        // Format the date as per your requirements
        // This is just a simple example, you can adjust it to match your desired format
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
    }


    private void drawPieChart(int width, int height, String catergory, String date) {

        // Retrieve unique names and their counts
        Map<String, Integer> nameCounts = dataAccess.getOrdersPieChartDaily(catergory, startDate, endDate);
        Log.d("pieChart", String.valueOf(nameCounts));
        List<String> uniqueNames = new ArrayList<>(nameCounts.keySet());
        int numSlices = uniqueNames.size();
        int[] data = new int[numSlices];
        int[] colors = new int[numSlices];

        for (int i = 0; i < numSlices; i++) {
            String name = uniqueNames.get(i);
            int count = nameCounts.get(name);

            data[i] = count;
            colors[i] = getColorForName(name); // Implement getColorForName() method based on your requirements
        }

        final float[] total = {0f}; // Declare as final array
        final float[] startAngle = {0f}; // Declare as final array

        for (int value : data) {
            total[0] += value;
        }

        pieChartContainer.removeAllViews(); // Clear pieChartContainer before adding Bitmap views
        final ArrayList<Bitmap> bitmaps = new ArrayList<>();

        for (int i = 0; i < data.length; i++) {
            float sweepAngle = 360f * ((float) data[i] / total[0]);

            if (width > 0 && height > 0) {
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);

                RectF rectF = new RectF(0, 0, width, height);
                Paint paint = new Paint();
                paint.setColor(colors[i]);
                Log.d("PieChart", "Width: " + width + ", Height: " + height);
                canvas.drawArc(rectF, startAngle[0], sweepAngle, true, paint);

                bitmaps.add(bitmap);

                if (bitmaps.size() == data.length) {
                    for (int j = 0; j < bitmaps.size(); j++) {
                        Bitmap b = bitmaps.get(j);
                        ImageView imageView = new ImageView(getContext());
                        imageView.setImageBitmap(b);
                        pieChartContainer.addView(imageView);
                        Log.d("PieChart", "Adding ImageView to pieChartContainer");

                        // Create a TextView for displaying the legend
                        TextView legendTextView = new TextView(getContext());
                        String name = uniqueNames.get(j);
                        int count = data[j];
                        float percentage = (float) count / total[0] * 100;

                        // Create a SpannableString to format the legend text
                        String legendText = "  " + name + " (" + String.format("%.1f", percentage) + "%)";
                        SpannableString spannableLegendText = new SpannableString(legendText);

                        // Create a colored dot with the corresponding color
                        Drawable dotDrawable = getResources().getDrawable(R.drawable.dot); // Replace "dot" with the name of your dot drawable
                        dotDrawable.setBounds(0, 0, 16, 16); // Adjust the size of the dot as desired
                        dotDrawable.setTint(colors[j]); // Set the dot color

                        // Create an ImageSpan with the colored dot drawable
                        ImageSpan dotSpan = new ImageSpan(dotDrawable, ImageSpan.ALIGN_BASELINE);

                        // Set the ImageSpan to the start of the legend text
                        spannableLegendText.setSpan(dotSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // Set the remaining text to black color
                        spannableLegendText.setSpan(new ForegroundColorSpan(Color.BLACK), 1, spannableLegendText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // Set the formatted text to the TextView
                        legendTextView.setText(spannableLegendText);
                        legendTextView.setTextSize(16); // Adjust the text size as desired
                        legendTextView.setTypeface(null, Typeface.BOLD); // Set the text style to bold
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        layoutParams.setMargins(0, 10, 0, 0);
                        legendTextView.setLayoutParams(layoutParams);

                        // Add the TextView to the legend container
                        legendContainer.addView(legendTextView);

                    }
                }
            }

            startAngle[0] += sweepAngle;
        }

        // Add a log statement to verify if this block is reached
        Log.d("PieChart", "Completed drawing the pie chart");
    }

    private int getColorForName(String name) {
        // Implement your logic to assign a color for each unique name
        // You can use a switch statement, if-else conditions, or any other method to map names to colors

        // For example, you can assign colors based on the hash code of the name
        int color = name.hashCode() | 0xFF000000; // Use the lower 24 bits of the hash code as the RGB values

        return color;
    }
    private List<String> getMenuCategories() {
        List<String> menuCategories = new ArrayList<>();
        menuCategories.add("Overall");
        menuCategories.add("Espresso");
        menuCategories.add("Add Ons");
        menuCategories.add("Frappe");
        menuCategories.add("Fruit Tea");
        menuCategories.add("Non Espresso");
        menuCategories.add("Sparkling Ade");
        menuCategories.add("Short Orders");
        return menuCategories;
    }

    private List<String> getDate() {
        List<String> menuCategories = new ArrayList<>();
        menuCategories.add("Daily");
        menuCategories.add("Weekly");
        menuCategories.add("Monthly");
        menuCategories.add("Quarterly");
        menuCategories.add("Yearly");
        return menuCategories;
    }
}
