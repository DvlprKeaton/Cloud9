package com.example.pos;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

public class ExcelExporter {

    public static void exportToExcel(Context context, List<String> data, List<String[]> cursorData, String fileName, String sDate, String eDate) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        if (fileName.contains("OrdersReport")) {
            // Create a header row
            Row headerRow = sheet.createRow(0);
            Cell headerCell = headerRow.createCell(0);
            String result = fileName.replace("OrdersReport ", "");
            headerCell.setCellValue("Most Ordered " + result);
        }

        if (data == null) {
            // Create data rows
            for (int i = 0; i < cursorData.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                String[] rowData = cursorData.get(i);
                for (int j = 0; j < rowData.length; j++) {
                    Cell dataCell = dataRow.createCell(j);
                    dataCell.setCellValue(rowData[j]);
                }
            }
        } else {
            // Create data rows
            for (int i = 0; i < data.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                Cell dataCell = dataRow.createCell(0);
                dataCell.setCellValue(data.get(i));
            }
        }

        Random random = new Random();
        int fileNumber = random.nextInt(100000);

        // Save the workbook to a file
        try {
            File file;
            String fileNameWithExtension;
            if (sDate == null && eDate == null) {
                fileNameWithExtension = fileNumber + "_" + fileName + ".xlsx";
            } else {
                fileNameWithExtension = fileNumber + "_" + fileName + "_" + sDate + "_" + eDate + ".xlsx";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Save to Documents folder on Android 10 and above
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileNameWithExtension);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
                Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
                try {
                    OutputStream outputStream = resolver.openOutputStream(uri);
                    if (outputStream != null) {
                        workbook.write(outputStream);
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Save to external storage on Android 9 and below
                File documentsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                if (!documentsFolder.exists()) {
                    documentsFolder.mkdirs();
                }
                file = new File(documentsFolder, fileNameWithExtension);
                FileOutputStream outputStream = new FileOutputStream(file);
                workbook.write(outputStream);
                outputStream.close();
            }

            // Display the file path in the toast message
            String filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + fileNameWithExtension;
            Toast.makeText(context, "Data exported to Excel\nFile path: " + filePath, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to export data to Excel", Toast.LENGTH_SHORT).show();
        }
    }


    private static void sendEmail(Context context, File file, String senderEmail, String receiverEmail) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // Use the 'mailto:' scheme

        // Set the sender email address
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{senderEmail});

        // Set the receiver email address
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{receiverEmail});

        // Create a content URI for the file
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

        // Grant read permission to the receiving app
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // You can customize the email subject and body if needed
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Excel File");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please find the attached Excel file.");

        try {
            context.startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show();
        }
    }




}
