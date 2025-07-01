package htl.steyr.wechselgeldapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class StartController extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_view);

        // Check storage permission and export DB immediately on app start
        checkPermissionAndExport();

        Button customerBTN = findViewById(R.id.customerBTN);
        Button sellerBTN = findViewById(R.id.sellerBTN);

        // Set button listeners to go to login/register screens for customer or seller
        customerBTN.setOnClickListener(view -> navigateTo("customer"));
        sellerBTN.setOnClickListener(view -> navigateTo("seller"));
    }

    // Check if app has permission to write to external storage, ask if not granted
    private void checkPermissionAndExport() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission from user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            // Permission already granted, export DB
            exportDatabaseToDownloads();
        }
    }

    // Handle user's response to permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, export DB
                exportDatabaseToDownloads();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Permission denied, cannot export DB.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Navigate to AuthController with the selected role (customer or seller)
    private void navigateTo(String role) {
        Intent intent = new Intent(StartController.this, AuthController.class);
        intent.putExtra("user_role", role);
        startActivity(intent);
    }

    // Export the app's database file to the device's public Downloads folder
    private void exportDatabaseToDownloads() {
        try {
            // Get the database file from the app's internal storage
            File dbFile = getDatabasePath("wechselgeld-db");

            // Get the public Downloads directory on external storage
            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!exportDir.exists()) exportDir.mkdirs();

            // Create the destination file path
            File exportFile = new File(exportDir, "wechselgeld-db");

            // Copy the database file to Downloads folder
            try (InputStream in = new FileInputStream(dbFile);
                 OutputStream out = new FileOutputStream(exportFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                // Show success message
                Toast.makeText(this, "Database exported to Downloads folder.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show error message if something went wrong
            Toast.makeText(this, "Error exporting the database.", Toast.LENGTH_SHORT).show();
        }
    }
}