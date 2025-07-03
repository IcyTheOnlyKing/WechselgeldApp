package htl.steyr.wechselgeldapp.UI;

    import android.Manifest;
    import android.annotation.SuppressLint;
    import android.app.Activity;
    import android.bluetooth.BluetoothDevice;
    import android.content.pm.PackageManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.widget.Button;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.RequiresPermission;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;

    import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
    import htl.steyr.wechselgeldapp.R;

    public class SellerUIController extends Activity implements Bluetooth.BluetoothCallback {
        private static final int PERMISSION_REQUEST_CODE = 1001;
        private Button scanDevicesButton;
        private TextView deviceListText;
        private Bluetooth bluetooth;
        private int deviceCount = 0;

        @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
        @SuppressLint("MissingInflatedId")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.seller_ui);

            scanDevicesButton = findViewById(R.id.scan_devices_button);
            deviceListText = findViewById(R.id.device_list_text);

            scanDevicesButton.setOnClickListener(v -> startDeviceScan());

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
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
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
                    deviceListText.setText("Bluetooth-Berechtigungen werden benötigt!");
                    Toast.makeText(this, "Berechtigungen erforderlich", Toast.LENGTH_LONG).show();
                }
            }
        }

        private void initBluetooth() {
            if (bluetooth.init()) {
                deviceListText.setText("Bereit zum Scannen nach Geräten");
                scanDevicesButton.setEnabled(true);
            } else {
                deviceListText.setText("Bluetooth-Fehler");
            }
        }

        @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
        private void startDeviceScan() {
            if (!bluetooth.isEnabled()) {
                Toast.makeText(this, "Bitte Bluetooth aktivieren", Toast.LENGTH_SHORT).show();
                return;
            }

            deviceCount = 0;
            deviceListText.setText("Scanne nach Geräten...\n");
            scanDevicesButton.setEnabled(false);

            if (!bluetooth.startScan()) {
                scanDevicesButton.setEnabled(true);
                deviceListText.setText("Scan konnte nicht gestartet werden");
            }
        }


        @Override
        public void onScanStarted() {
            deviceListText.append("Suche gestartet...\n");
        }

        @Override
        public void onDeviceFound(BluetoothDevice device) {
            deviceCount++;
            String name = "Unbekannt";

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                if (device.getName() != null) name = device.getName();
            }

            deviceListText.append(String.format("(%d) %s (%s)\n", deviceCount, name, device.getAddress()));
            String finalName = name;
            deviceListText.setOnClickListener(v -> {
                if (hasPermissions()) {
                    try {
                        device.createBond();
                        Toast.makeText(this, "Kopplungsanfrage gesendet an " + finalName, Toast.LENGTH_SHORT).show();
                    } catch (SecurityException e) {
                        Toast.makeText(this, "Berechtigung für Geräteverbindung fehlt", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onScanFinished() {
            deviceListText.append(String.format("\n=== %d Geräte gefunden ===\n", deviceCount));
            scanDevicesButton.setEnabled(true);
        }

        @Override
        public void onError(String error) {
            deviceListText.append("FEHLER: " + error + "\n");
            scanDevicesButton.setEnabled(true);
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