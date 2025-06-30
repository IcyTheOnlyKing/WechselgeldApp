package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import htl.steyr.wechselgeldapp.R;

public class BluetoothTestActivity extends AppCompatActivity implements Bluetooth.BluetoothCallback {
    private Bluetooth bluetooth;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test); // ➜ Name muss mit XML-Datei übereinstimmen

        bluetooth = new Bluetooth(this, this);

        Button scanButton = findViewById(R.id.scan_button);
        statusText = findViewById(R.id.status_text);

        scanButton.setOnClickListener(v -> {
            bluetooth.initializeBluetooth();
            bluetooth.startDiscovery();
            statusText.setText("Suche nach Geräten...");
        });
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        runOnUiThread(() -> {
            String name = (device.getName() != null) ? device.getName() : "Unbekanntes Gerät";
            statusText.append("\nGefunden: " + name);
        });
    }

    @Override
    public void onDiscoveryFinished() {
        runOnUiThread(() -> statusText.append("\nSuche beendet"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetooth.cleanup();
    }
}
