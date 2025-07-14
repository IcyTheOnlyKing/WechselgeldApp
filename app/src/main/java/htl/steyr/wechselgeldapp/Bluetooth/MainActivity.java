package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import htl.steyr.wechselgeldapp.Backup.BackupManager;
import htl.steyr.wechselgeldapp.R;

public class MainActivity extends Activity implements Bluetooth.BluetoothCallback {
    private BackupManager backupManager;
    private String userEmail = "user@example.com";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    // UI Elemente
    private Button scanDevicesButton;
    private TextView deviceStatusText;
    private RecyclerView deviceRecyclerView;
    private LinearLayout bottomIconBar;

    // Bluetooth
    private Bluetooth bluetooth;
    private BluetoothDeviceAdapter deviceAdapter;

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_ui);

        initViews();
        setupClickListeners();
        initBluetooth();
    }

    private void initViews() {
        scanDevicesButton = findViewById(R.id.scan_devices_button);
        deviceStatusText = findViewById(R.id.device_status_text);
        deviceRecyclerView = findViewById(R.id.device_list);
        bottomIconBar = findViewById(R.id.bottom_icon_bar);

        // RecyclerView Setup
        deviceAdapter = new BluetoothDeviceAdapter(this::onDeviceClick);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceRecyclerView.setAdapter(deviceAdapter);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void setupClickListeners() {
        // Scan Button
        scanDevicesButton.setOnClickListener(v -> startDeviceScan());

        // Bottom Navigation - Koppeln Button
        LinearLayout pairingButton = (LinearLayout) bottomIconBar.getChildAt(1);
        pairingButton.setOnClickListener(v -> openPairingActivity());

        // Bottom Navigation - Start Button
        LinearLayout startButton = (LinearLayout) bottomIconBar.getChildAt(0);
        startButton.setOnClickListener(v -> {
            Toast.makeText(this, "Start ausgewählt", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation - Transaktionen Button
        LinearLayout transactionsButton = (LinearLayout) bottomIconBar.getChildAt(2);
        transactionsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Transaktionen ausgewählt", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation - Einstellungen Button
        LinearLayout settingsButton = (LinearLayout) bottomIconBar.getChildAt(3);
        settingsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Einstellungen ausgewählt", Toast.LENGTH_SHORT).show();
        });
    }

    private void openPairingActivity() {
        Intent intent = new Intent(this, BluetoothPairingActivity.class);
        startActivity(intent);
    }

    private void initBluetooth() {
        bluetooth = new Bluetooth(this, this);

        if (hasPermissions()) {
            initializeBluetoothAdapter();
        } else {
            requestPermissions();
        }
    }

    private void initializeBluetoothAdapter() {
        if (bluetooth.init()) {
            deviceStatusText.setText("Bereit zum Scannen nach Bluetooth-Geräten");
            scanDevicesButton.setEnabled(true);
        } else {
            deviceStatusText.setText("Bluetooth-Fehler");
            scanDevicesButton.setEnabled(false);
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void startDeviceScan() {
        if (!bluetooth.isEnabled()) {
            Toast.makeText(this, "Bitte Bluetooth aktivieren", Toast.LENGTH_SHORT).show();
            return;
        }

        deviceStatusText.setText("Scanne nach Geräten...");
        scanDevicesButton.setEnabled(false);
        deviceAdapter.clearDevices();

        if (!bluetooth.startScan()) {
            scanDevicesButton.setEnabled(true);
            deviceStatusText.setText("Scan konnte nicht gestartet werden");
        }
    }

    private void onDeviceClick(BluetoothDevice device) {
        if (hasPermissions()) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    String deviceName = device.getName() != null ? device.getName() : "Unbekanntes Gerät";
                    device.createBond();
                    Toast.makeText(this, "Kopplungsanfrage gesendet an " + deviceName, Toast.LENGTH_SHORT).show();
                }
            } catch (SecurityException e) {
                Toast.makeText(this, "Berechtigung für Geräteverbindung fehlt", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Bluetooth Callback Methods
    @Override
    public void onScanStarted() {
        runOnUiThread(() -> {
            deviceStatusText.setText("Suche nach Geräten...");
        });
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        runOnUiThread(() -> {
            deviceAdapter.addDevice(device);
            deviceStatusText.setText("Geräte gefunden: " + deviceAdapter.getItemCount());
        });
    }

    @Override
    public void onScanFinished() {
        runOnUiThread(() -> {
            scanDevicesButton.setEnabled(true);
            int deviceCount = deviceAdapter.getItemCount();
            deviceStatusText.setText(deviceCount + " Geräte gefunden");
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            deviceStatusText.setText("Fehler: " + error);
            scanDevicesButton.setEnabled(true);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    // Permission Handling
    private String[] getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
        return new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    private boolean hasPermissions() {
        for (String permission : getPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, getPermissions(), PERMISSION_REQUEST_CODE);
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
                initializeBluetoothAdapter();
            } else {
                deviceStatusText.setText("Bluetooth-Berechtigungen werden benötigt!");
                Toast.makeText(this, "Berechtigungen erforderlich", Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetooth != null) {
            bluetooth.cleanup();
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    protected void onPause() {
        super.onPause();
        if (bluetooth != null) {
            bluetooth.stopScan();
        }
    }
}
