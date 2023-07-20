package com.example.pos;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PrintTask extends AsyncTask<Void, Void, Void> {
    private BluetoothSocket socket;
    private OutputStream outputStreamPrint;

    private String receiptID;
    private String receiptNumber;
    private String orderQuantity;
    private String orderType;
    private String orderDiscount;
    private String orderDiscountType;
    private String orderPaymentType;
    private String orderPayment;
    private String orderTotal;
    private String orderChange;
    private String totalPaymentValue;
    private String addedBy;
    private String addedAt;
    private String Username;
    private Context context;
    private Bitmap qrCode;

    public PrintTask(Context context, BluetoothSocket socket, OutputStream outputStreamPrint, Bitmap qrCode) {
        this.socket = socket;
        this.outputStreamPrint = outputStreamPrint;
        this.context = context;

       this.qrCode = qrCode;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            int paperWidth = 380; // Update with the width of your paper size

            // Compute the height dynamically based on the content
            int paperHeight = computePaperHeight(qrCode, paperWidth);

            printPic printPic1 = printPic.getInstance();
            printPic1.init(qrCode, paperWidth, paperHeight);
            byte[] bitmapdata = printPic1.printDraw();
            outputStreamPrint.write(bitmapdata);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int computePaperHeight(Bitmap qrCode, int paperWidth) {
        // Compute the height based on the content and desired paper width
        // You can implement your own logic here to calculate the height dynamically
        // For example, you can use the aspect ratio of the bitmap to calculate the height

        float aspectRatio = (float) qrCode.getHeight() / qrCode.getWidth();
        int calculatedHeight = (int) (paperWidth * aspectRatio) + 100;

        // Ensure the calculated height is within your desired range
        int minHeight = 500; // Adjust this value based on your needs
        return Math.max(minHeight, calculatedHeight);
    }



    private Bitmap captureView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }


    @Override
    protected void onPostExecute(Void result) {
        // Perform any post-execution tasks if needed
    }
}