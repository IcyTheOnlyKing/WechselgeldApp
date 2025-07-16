package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

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

import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothDeviceAdapter;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class ConnectFragment extends BaseFragment implements Bluetooth.BluetoothCallback {

    private Bluetooth bluetooth;
    private BluetoothDeviceAdapter deviceAdapter;
    private RecyclerView deviceRecyclerView;
    private TextView statusText;
    private ProgressBar progressBar;
    private Button scanButton;
    private static final int PERMISSION_REQUEST_CODE = 1003;

    /**
     * Called when the fragment's view is being created.
     * Initializes UI components, Bluetooth instance, and checks permissions.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_connect, container, false);


        deviceAdapter = new BluetoothDeviceAdapter(this::onDeviceClick);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        deviceRecyclerView.setAdapter(deviceAdapter);

        bluetooth = new Bluetooth(requireContext(), this);

        scanButton.setOnClickListener(v -> startDeviceScan());

        if (hasPermissions()) {
            initializeBluetooth();
        } else {
            requestPermissions();
        }

        return view;
    }

    /**
     * Called when a Bluetooth device item is clicked.
     * Attempts to initiate pairing with the device.
     */
    private void onDeviceClick(BluetoothDevice device) {
        if (hasPermissions()) {
            try {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    String deviceName = (device.getName() != null) ? device.getName() : "Unknown device";
                    device.createBond();
                    Toast.makeText(requireContext(), "Pairing request sent to " + deviceName, Toast.LENGTH_SHORT).show();
                }
            } catch (SecurityException e) {
                Toast.makeText(requireContext(), "Missing permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initializes the Bluetooth adapter and updates the UI accordingly.
     */
    private void initializeBluetooth() {
        if (bluetooth.init()) {
            bluetooth.startServer();
            statusText.setText("Bereit zum Scannen");
            scanButton.setEnabled(true);
        } else {
            statusText.setText("Bluetooth Fehler");
            scanButton.setEnabled(false);
        }
    }

    /**
     * Starts scanning for Bluetooth devices and updates UI.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void startDeviceScan() {
        if (!bluetooth.isEnabled()) {
            Toast.makeText(requireContext(), "Bitte aktivieren Sie Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Scannen...");

        scanButton.setEnabled(false);
        deviceAdapter.clearDevices();

        if (!bluetooth.startScan()) {
            scanButton.setEnabled(true);
            statusText.setText("Scan fehlgeschlagen!");
        }
    }

    /**
     * Called when scanning is started.
     * Updates UI to show scan is in progress.
     */
    @Override
    public void onScanStarted() {
        requireActivity().runOnUiThread(() -> {
            statusText.setText("Scannen...");

        });
    }

    /**
     * Called when a Bluetooth device is found.
     * Adds the device to the list and updates UI.
     */
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        requireActivity().runOnUiThread(() -> {
            deviceAdapter.addDevice(device);
            statusText.setText("Geräte: " + deviceAdapter.getItemCount());
        });
    }

    /**
     * Called when scanning is finished.
     * Updates UI and enables the scan button.
     */
    @Override
    public void onScanFinished() {
        requireActivity().runOnUiThread(() -> {
            scanButton.setEnabled(true);
            statusText.setText(deviceAdapter.getItemCount() + " Geräte gefunden");
        });
    }

    /**
     * Called when a Bluetooth error occurs.
     * Displays the error message.
     */
    @Override
    public void onError(String error) {
        requireActivity().runOnUiThread(() -> {
            scanButton.setEnabled(true);
            statusText.setText("Fehler: " + error);
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onConnectionSuccess(BluetoothDevice device) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), "Verbunden mit " + device.getName(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDataSent(boolean success) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), success ? "Gesendet" : "Sendefehler", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDataReceived(UserData data) {
        requireActivity().runOnUiThread(() -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Daten empfangen")
                    .setMessage("Name: " + data.getUsername() + "\nGuthaben: " + data.getTotalAmount())
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    public void onDisconnected() {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), "Verbindung getrennt", Toast.LENGTH_SHORT).show());
    }

    /**
     * Returns the necessary permissions depending on the Android version.
     */
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

    /**
     * Checks whether all required permissions are granted.
     */
    private boolean hasPermissions() {
        for (String permission : getPermissions()) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Requests the required permissions.
     */
    private void requestPermissions() {
        requestPermissions(getPermissions(), PERMISSION_REQUEST_CODE);
    }

    /**
     * Handles the result of the permission request dialog.
     */
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
                Toast.makeText(requireContext(), "Zugriff verweigert", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Called when the fragment is paused.
     * Stops any ongoing Bluetooth scan.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    public void onPause() {
        super.onPause();
        if (bluetooth != null) {
            bluetooth.stopScan();
        }
    }

    /**
     * Called when the fragment is destroyed.
     * Cleans up Bluetooth resources.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetooth != null) {
            bluetooth.cleanup();
        }
    }

    @Override
    public String getTitle() {
        return "Koppeln";
    }
}
