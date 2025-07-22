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
 * Startcontroller für die Wechselgeld-App.
 * Zeigt bei App-Start die Rollenwahl an, sofern der Nutzer nicht eingeloggt ist.
 * Fragt notwendige Bluetooth- und Standortberechtigungen beim ersten Start ab.
 */
public class StartController extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI anzeigen, bevor Berechtigungsdialog kommt
        setContentView(R.layout.start_view);

        if (!hasAllPermissions()) {
            showPermissionDialogAndRequest();
        } else {
            proceedAfterPermission();
        }
    }

    /**
     * Gibt die zur Laufzeit abzufragenden Berechtigungen je nach Android-Version zurück.
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

    private void showPermissionDialogAndRequest() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Berechtigungen erforderlich")
                .setMessage("Diese App benötigt Bluetooth- und Standortrechte, um mit Geräten in der Nähe zu kommunizieren.")
                .setPositiveButton("OK", (dialog, which) ->
                        ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQUEST_CODE))
                .setNegativeButton("Abbrechen", (dialog, which) -> {
                    Toast.makeText(this, "Ohne Berechtigungen kann die App nicht funktionieren.", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(this, "Alle Berechtigungen müssen akzeptiert werden.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            proceedAfterPermission();
        }
    }

    /**
     * Wird aufgerufen, wenn alle Berechtigungen erteilt wurden.
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
     * Initialisiert die Buttons zur Rollenwahl (Customer/Seller).
     */
    private void setupButtons() {
        Button customerBTN = findViewById(R.id.customerBTN);
        Button sellerBTN = findViewById(R.id.sellerBTN);

        customerBTN.setOnClickListener(view -> navigateTo("customer"));
        sellerBTN.setOnClickListener(view -> navigateTo("seller"));
    }

    private void navigateTo(String role) {
        Intent intent = new Intent(this, AuthController.class);
        intent.putExtra("user_role", role);
        startActivity(intent);
    }
}