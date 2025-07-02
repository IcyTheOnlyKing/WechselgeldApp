package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import htl.steyr.wechselgeldapp.R;

public class BluetoothTestActivity extends AppCompatActivity implements Bluetooth.BluetoothCallback {
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private Bluetooth bluetooth;
    private TextView statusText;
    private Button scanButton, stopButton;
    private int deviceCount = 0;

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test);

        scanButton = findViewById(R.id.scan_button);
        stopButton = findViewById(R.id.stop_button);
        statusText = findViewById(R.id.status_text);

        scanButton.setOnClickListener(v -> startScan());
        stopButton.setOnClickListener(v -> stopScan());
        stopButton.setEnabled(false);

        bluetooth = new Bluetooth(this, this);

        if (hasPermissions()) {
            initBluetooth();
        } else {
            requestPermissions();
        }
    }

    private String[] getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION};
        return new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION};
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
                initBluetooth();
            } else {
                statusText.setText("Bluetooth-Berechtigungen werden benötigt!");
                Toast.makeText(this, "Berechtigungen erforderlich", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initBluetooth() {
        if (bluetooth.init()) {
            statusText.setText("Bereit zum Scannen");
            scanButton.setEnabled(true);
        } else {
            statusText.setText("Bluetooth-Fehler");
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void startScan() {
        if (!bluetooth.isEnabled()) {
            Toast.makeText(this, "Bluetooth aktivieren", Toast.LENGTH_SHORT).show();
            return;
        }

        deviceCount = 0;
        statusText.setText("Scanne...\n");

        if (bluetooth.startScan()) {
            scanButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void stopScan() {
        bluetooth.stopScan();
        scanButton.setEnabled(true);
        stopButton.setEnabled(false);
        statusText.append("\nScan gestoppt");
    }

    @Override
    public void onScanStarted() {
        statusText.append("Suche gestartet...\n");
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        deviceCount++;
        String name = "Unbekannt";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            if (device.getName() != null) name = device.getName();
            else name = "Unbekannt";
        }
        statusText.append(String.format("(%d) %s (%s)\n", deviceCount, name, device.getAddress()));
    }

    @Override
    public void onScanFinished() {
        statusText.append(String.format("\n=== %d Geräte gefunden ===\n", deviceCount));
        scanButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    @Override
    public void onError(String error) {
        statusText.append("FEHLER: " + error + "\n");
        scanButton.setEnabled(true);
        stopButton.setEnabled(false);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetooth != null) bluetooth.cleanup();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    protected void onPause() {
        super.onPause();
        if (bluetooth != null) bluetooth.stopScan();
    }
}