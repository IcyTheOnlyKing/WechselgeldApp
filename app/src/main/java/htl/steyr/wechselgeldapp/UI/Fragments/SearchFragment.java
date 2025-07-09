package htl.steyr.wechselgeldapp.UI.Fragments;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Objects;
import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothDeviceAdapter;
import htl.steyr.wechselgeldapp.R;

public class SearchFragment extends Fragment implements Bluetooth.BluetoothCallback {

    private Bluetooth bluetooth;
    private BluetoothDeviceAdapter deviceAdapter;
    private RecyclerView deviceRecyclerView;
    private TextView statusText;
    private ProgressBar progressBar;
    private Button scanButton;
    private static final int PERMISSION_REQUEST_CODE = 1003;

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // UI-Elemente initialisieren
        statusText = view.findViewById(R.id.status_text);
        progressBar = view.findViewById(R.id.progress_bar);
        scanButton = view.findViewById(R.id.scan_button);
        deviceRecyclerView = view.findViewById(R.id.device_list);

        // RecyclerView Setup
        deviceAdapter = new BluetoothDeviceAdapter(this::onDeviceClick);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        deviceRecyclerView.setAdapter(deviceAdapter);

        // Bluetooth initialisieren
        bluetooth = new Bluetooth(requireContext(), this);

        // Scan-Button Listener
        scanButton.setOnClickListener(v -> startDeviceScan());

        // Berechtigungen prüfen
        if (hasPermissions()) {
            initializeBluetooth();
        } else {
            requestPermissions();
        }

        return view;
    }

    private void onDeviceClick(BluetoothDevice device) {
        if (hasPermissions()) {
            try {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    String deviceName = (device.getName() != null) ? device.getName() : "Unbekanntes Gerät";
                    device.createBond();
                    Toast.makeText(requireContext(), "Kopplungsanfrage gesendet an " + deviceName, Toast.LENGTH_SHORT).show();
                }
            } catch (SecurityException e) {
                Toast.makeText(requireContext(), "Berechtigung fehlt", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeBluetooth() {
        if (bluetooth.init()) {
            statusText.setText("Bereit zum Scannen");
            scanButton.setEnabled(true);
        } else {
            statusText.setText("Bluetooth-Fehler");
            scanButton.setEnabled(false);
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void startDeviceScan() {
        if (!bluetooth.isEnabled()) {
            Toast.makeText(requireContext(), "Bluetooth aktivieren", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Scanne...");
        progressBar.setVisibility(View.VISIBLE);
        scanButton.setEnabled(false);
        deviceAdapter.clearDevices();

        if (!bluetooth.startScan()) {
            scanButton.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            statusText.setText("Scan fehlgeschlagen");
        }
    }

    // Bluetooth Callbacks
    @Override
    public void onScanStarted() {
        requireActivity().runOnUiThread(() -> {
            statusText.setText("Scan läuft...");
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        requireActivity().runOnUiThread(() -> {
            deviceAdapter.addDevice(device);
            statusText.setText("Geräte: " + deviceAdapter.getItemCount());
        });
    }

    @Override
    public void onScanFinished() {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            scanButton.setEnabled(true);
            statusText.setText(deviceAdapter.getItemCount() + " Geräte gefunden");
        });
    }

    @Override
    public void onError(String error) {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            scanButton.setEnabled(true);
            statusText.setText("Fehler: " + error);
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    // Berechtigungs-Handling
    private String[] getPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
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
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        requestPermissions(getPermissions(), PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                initializeBluetooth();
            } else {
                statusText.setText("Berechtigungen benötigt!");
                Toast.makeText(requireContext(), "Berechtigungen verweigert", Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    public void onPause() {
        super.onPause();
        if (bluetooth != null) {
            bluetooth.stopScan();
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetooth != null) {
            bluetooth.cleanup();
        }
    }
}