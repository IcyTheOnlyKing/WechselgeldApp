package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.Manifest;
import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import htl.steyr.wechselgeldapp.Backup.UserData;
import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothDeviceAdapter;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class ConnectFragment extends BaseFragment implements Bluetooth.BluetoothCallback {

    private Bluetooth bluetooth;
    private BluetoothDeviceAdapter deviceAdapter;
    private TextView statusText;
    private Button scanButton;
    private ProgressBar progressBar;
    private static final int PERMISSION_REQUEST_CODE = 1003;

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_connect, container, false);

        RecyclerView deviceRecyclerView = view.findViewById(R.id.recycler_devices);
        statusText = view.findViewById(R.id.status_text);
        progressBar = view.findViewById(R.id.progress_bar);
        scanButton = view.findViewById(R.id.btn_scan);

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

    private void onDeviceClick(BluetoothDevice device) {
        if (!isUIActive()) return;
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            String deviceName = (device.getName() != null) ? device.getName() : "Unknown device";
            device.createBond();
            Toast.makeText(requireContext(), "Pairing request sent to " + deviceName, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeBluetooth() {
        if (bluetooth != null && bluetooth.init()) {
            bluetooth.startServer();
            statusText.setText("Bereit zum Scannen");
            scanButton.setEnabled(true);
        } else {
            statusText.setText("Bluetooth Fehler");
            scanButton.setEnabled(false);
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void startDeviceScan() {
        if (!isUIActive()) return;

        Toast.makeText(requireContext(), "Scan gestartet", Toast.LENGTH_SHORT).show();

        if (bluetooth == null) {
            Toast.makeText(requireContext(), "Bluetooth nicht initialisiert!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetooth.isEnabled()) {
            Toast.makeText(requireContext(), "Bitte aktivieren Sie Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Scannen...");
        scanButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        deviceAdapter.clearDevices();

        if (!bluetooth.startScan()) {
            scanButton.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            statusText.setText("Scan fehlgeschlagen!");
            Toast.makeText(requireContext(), "Scan-Start fehlgeschlagen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onScanStarted() {
        if (!isUIActive()) return;
        requireActivity().runOnUiThread(() -> {
            statusText.setText("Scannen...");
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        if (!isUIActive()) return;
        requireActivity().runOnUiThread(() -> {
            deviceAdapter.addDevice(device);
            statusText.setText("Geräte: " + deviceAdapter.getItemCount());
        });
    }

    @Override
    public void onScanFinished() {
        if (!isUIActive()) return;
        requireActivity().runOnUiThread(() -> {
            scanButton.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            statusText.setText(deviceAdapter.getItemCount() + " Geräte gefunden");
        });
    }

    @Override
    public void onError(String error) {
        if (!isUIActive()) return;
        requireActivity().runOnUiThread(() -> {
            scanButton.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            statusText.setText("Fehler: " + error);
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onConnectionSuccess(BluetoothDevice device) {
        if (!isUIActive()) return;
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), "Verbunden mit " + device.getName(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDataSent(boolean success) {
        if (!isUIActive()) return;
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), success ? "Gesendet" : "Sendefehler", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDataReceived(UserData data) {
        if (!isUIActive()) return;
        requireActivity().runOnUiThread(() -> {
            StringBuilder msg = new StringBuilder();
            msg.append("Name: ").append(data.getUsername());
            msg.append("\nGuthaben: ").append(data.getTotalAmount());
            if (data.getSellerName() != null) {
                msg.append("\nVerkäufer: ").append(data.getSellerName());
            }
            if (data.getTransactionAmount() != 0) {
                msg.append("\nBetrag: ").append(data.getTransactionAmount());
            }
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Daten empfangen")
                    .setMessage(msg.toString())
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    public void onDisconnected() {
        if (!isUIActive()) return;
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), "Verbindung getrennt", Toast.LENGTH_SHORT).show());
    }

    private boolean isUIActive() {
        return isAdded() && getActivity() != null && !isRemoving();
    }

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
                if (isUIActive()) {
                    statusText.setText("Berechtigungen benötigt!");
                    Toast.makeText(requireContext(), "Zugriff verweigert", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    public void onPause() {
        super.onPause();
        cleanupBluetooth();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanupBluetooth();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void cleanupBluetooth() {
        try {
            if (bluetooth != null) {
                bluetooth.stopScan();
                bluetooth.cleanup();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        bluetooth = null;
        deviceAdapter = null;
        scanButton = null;
        statusText = null;
        progressBar = null;
    }

    @Override
    public String getTitle() {
        return "Koppeln";
    }
}
