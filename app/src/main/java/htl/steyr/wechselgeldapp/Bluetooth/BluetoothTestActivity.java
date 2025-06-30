package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import htl.steyr.wechselgeldapp.R;

public class BluetoothTestActivity extends AppCompatActivity implements Bluetooth.BluetoothCallback {
    private static final String TAG = "BluetoothTestActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private Bluetooth bluetooth;
    private TextView statusText;
    private Button scanButton;
    private Button stopButton;
    private int deviceCount = 0;

    // Liste der benötigten Berechtigungen je nach Android-Version
    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[] {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            return new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test);

        initializeViews();
        bluetooth = new Bluetooth(this, this);

        // Prüfe Berechtigungen beim Start
        if (checkPermissions()) {
            initializeBluetooth();
        } else {
            requestPermissions();
        }
    }

    private void initializeViews() {
        scanButton = findViewById(R.id.scan_button);
        stopButton = findViewById(R.id.stop_button);
        statusText = findViewById(R.id.status_text);

        scanButton.setOnClickListener(v -> startBluetoothScan());
        stopButton.setOnClickListener(v -> stopBluetoothScan());

        // Stop-Button initial deaktivieren
        stopButton.setEnabled(false);
    }

    private boolean checkPermissions() {
        String[] permissions = getRequiredPermissions();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Berechtigung fehlt: " + permission);
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        String[] permissions = getRequiredPermissions();
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.d(TAG, "Alle Berechtigungen erteilt");
                initializeBluetooth();
                updateStatus("Berechtigungen erteilt. Bereit zum Scannen.");
            } else {
                Log.w(TAG, "Nicht alle Berechtigungen erteilt");
                updateStatus("Bluetooth-Berechtigungen werden benötigt!");
                Toast.makeText(this, "Bluetooth-Berechtigungen sind erforderlich", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeBluetooth() {
        if (bluetooth.initializeBluetooth()) {
            updateStatus("Bluetooth initialisiert. Bereit zum Scannen.");
            scanButton.setEnabled(true);
        } else {
            updateStatus("Fehler bei Bluetooth-Initialisierung");
            scanButton.setEnabled(false);
        }
    }

    private void startBluetoothScan() {
        if (!bluetooth.isBluetoothEnabled()) {
            updateStatus("Bluetooth ist nicht aktiviert!");
            Toast.makeText(this, "Bitte aktivieren Sie Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        deviceCount = 0;
        updateStatus("Starte Bluetooth-Scan...\n");

        if (bluetooth.startDiscovery()) {
            scanButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            updateStatus("Fehler beim Starten des Scans");
        }
    }

    private void stopBluetoothScan() {
        bluetooth.stopDiscovery();
        scanButton.setEnabled(true);
        stopButton.setEnabled(false);
        updateStatus(statusText.getText() + "\nScan manuell gestoppt.");
    }

    private void updateStatus(String message) {
        runOnUiThread(() -> statusText.setText(message));
    }

    private void appendStatus(String message) {
        runOnUiThread(() -> statusText.append(message));
    }

    @Override
    public void onDiscoveryStarted() {
        appendStatus("Suche gestartet...\n");
        Log.d(TAG, "Discovery gestartet");
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        deviceCount++;
        String deviceInfo = getDeviceInfo(device);
        appendStatus(String.format("(%d) %s\n", deviceCount, deviceInfo));
        Log.d(TAG, "Gerät gefunden: " + deviceInfo);
    }

    @Override
    public void onDiscoveryFinished() {
        appendStatus(String.format("\n=== Scan beendet ===\nInsgesamt %d Geräte gefunden\n", deviceCount));

        scanButton.setEnabled(true);
        stopButton.setEnabled(false);

        // Zeige Zusammenfassung
        List<BluetoothDevice> devices = bluetooth.getDeviceList();
        appendStatus(String.format("Geräte in Liste: %d\n", devices.size()));

        Log.d(TAG, "Discovery beendet. " + deviceCount + " Geräte gefunden");
    }

    @Override
    public void onBluetoothError(String error) {
        appendStatus("FEHLER: " + error + "\n");
        Log.e(TAG, "Bluetooth Fehler: " + error);

        scanButton.setEnabled(true);
        stopButton.setEnabled(false);

        Toast.makeText(this, "Bluetooth-Fehler: " + error, Toast.LENGTH_LONG).show();
    }

    private String getDeviceInfo(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return device.getAddress();
        }

        String name = device.getName();
        String address = device.getAddress();
        int bondState = device.getBondState();

        String bondStateText;
        switch (bondState) {
            case BluetoothDevice.BOND_BONDED:
                bondStateText = "Gekoppelt";
                break;
            case BluetoothDevice.BOND_BONDING:
                bondStateText = "Kopplung läuft";
                break;
            default:
                bondStateText = "Nicht gekoppelt";
                break;
        }

        if (name != null && !name.isEmpty()) {
            return String.format("%s (%s) - %s", name, address, bondStateText);
        } else {
            return String.format("Unbekannt (%s) - %s", address, bondStateText);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetooth != null) {
            bluetooth.cleanup();
        }
        Log.d(TAG, "Activity zerstört");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stoppe Scan wenn Activity pausiert wird
        if (bluetooth != null && bluetooth.isDiscovering()) {
            bluetooth.stopDiscovery();
        }
    }
}