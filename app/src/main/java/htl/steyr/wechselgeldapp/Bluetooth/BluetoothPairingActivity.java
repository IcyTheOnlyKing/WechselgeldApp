package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.R;

public class BluetoothPairingActivity extends Activity implements Bluetooth.BluetoothCallback {
    private static final int PERMISSION_REQUEST_CODE = 1002;

    private TextView statusText;
    private ProgressBar progressBar;
    private Button scanAgainButton;
    private RecyclerView deviceRecyclerView;

    private Bluetooth bluetooth;
    private BluetoothDeviceAdapter deviceAdapter;

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        initViews();
        setupClickListeners();
        initBluetooth();
    }

    private void initViews() {
        statusText = findViewById(R.id.status_text);
        scanAgainButton = findViewById(R.id.scan_button);
        deviceRecyclerView = findViewById(R.id.device_list);

        // RecyclerView Setup
        deviceAdapter = new BluetoothDeviceAdapter(this::onDeviceClick);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceRecyclerView.setAdapter(deviceAdapter);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void setupClickListeners() {
        scanAgainButton.setOnClickListener(v -> startDeviceScan());
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void initBluetooth() {
        bluetooth = new Bluetooth(this, this);

        if (hasPermissions()) {
            initializeBluetoothAdapter();
        } else {
            requestPermissions();
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void initializeBluetoothAdapter() {
        if (bluetooth.init()) {
            statusText.setText("Bereit zum Scannen");
            scanAgainButton.setEnabled(true);
            startDeviceScan(); // Automatisch scannen beim Start
        } else {
            statusText.setText("Bluetooth-Fehler");
            scanAgainButton.setEnabled(false);
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void startDeviceScan() {
        if (!bluetooth.isEnabled()) {
            Toast.makeText(this, "Bitte Bluetooth aktivieren", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Scanne nach Geräten...");
        progressBar.setVisibility(View.VISIBLE);
        scanAgainButton.setEnabled(false);
        deviceAdapter.clearDevices();

        if (!bluetooth.startScan()) {
            scanAgainButton.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            statusText.setText("Scan konnte nicht gestartet werden");
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
            statusText.setText("Suche läuft...");
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        runOnUiThread(() -> {
            deviceAdapter.addDevice(device);
            statusText.setText("Geräte gefunden: " + deviceAdapter.getItemCount());
        });
    }

    @Override
    public void onScanFinished() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            scanAgainButton.setEnabled(true);
            int deviceCount = deviceAdapter.getItemCount();
            statusText.setText(deviceCount + " Geräte gefunden - Tippen zum Koppeln");
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            statusText.setText("Fehler: " + error);
            scanAgainButton.setEnabled(true);
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

    @SuppressLint("SetTextI18n")
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
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
                statusText.setText("Bluetooth-Berechtigungen werden benötigt!");
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
