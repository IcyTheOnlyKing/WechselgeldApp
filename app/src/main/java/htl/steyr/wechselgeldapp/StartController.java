package htl.steyr.wechselgeldapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import htl.steyr.wechselgeldapp.UI.CustomerUIController;
import htl.steyr.wechselgeldapp.UI.SellerUIController;

/**
 * Start controller for the Wechselgeld app.
 * Displays role selection at app launch if the user is not logged in.
 * Requests necessary Bluetooth and location permissions on first start.
 */
public class StartController extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show UI before permission dialog appears
        setContentView(R.layout.start_view);

        if (!hasAllPermissions()) {
            showPermissionDialogAndRequest();
        } else {
            proceedAfterPermission();
        }
    }

    /**
     * Returns the permissions that need to be requested at runtime,
     * depending on the Android version.
     */
    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            return new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
    }

    private boolean hasAllPermissions() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Shows a dialog explaining the required permissions and then requests them.
     */
    private void showPermissionDialogAndRequest() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("This app requires Bluetooth and location permissions to communicate with nearby devices.")
                .setPositiveButton("OK", (dialog, which) ->
                        ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQUEST_CODE))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "The app cannot function without permissions.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "All permissions must be granted.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            proceedAfterPermission();
        }
    }

    /**
     * Called when all permissions have been granted.
     * Navigates to the appropriate UI depending on login status and user role.
     */
    private void proceedAfterPermission() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        String role = prefs.getString("user_role", "");

        if (isLoggedIn) {
            if ("seller".equals(role)) {
                startActivity(new Intent(this, SellerUIController.class));
            } else if ("customer".equals(role)) {
                startActivity(new Intent(this, CustomerUIController.class));
            }
            finish();
        } else {
            setupButtons();
        }
    }

    /**
     * Initializes the buttons for role selection (Customer/Seller).
     */
    private void setupButtons() {
        Button customerBTN = findViewById(R.id.customerBTN);
        Button sellerBTN = findViewById(R.id.sellerBTN);

        customerBTN.setOnClickListener(view -> navigateTo("customer"));
        sellerBTN.setOnClickListener(view -> navigateTo("seller"));
    }

    /**
     * Starts the AuthController and passes the selected role as an intent extra.
     *
     * @param role The selected user role ("customer" or "seller")
     */
    private void navigateTo(String role) {
        Intent intent = new Intent(this, AuthController.class);
        intent.putExtra("user_role", role);
        startActivity(intent);
    }
}
