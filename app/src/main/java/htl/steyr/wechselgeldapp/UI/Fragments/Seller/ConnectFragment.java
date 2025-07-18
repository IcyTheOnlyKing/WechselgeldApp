package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class ConnectFragment extends BaseFragment {

    private static final long SCAN_TIMEOUT = 10000;

    private BluetoothAdapter bluetoothAdapter;
    private final List<BluetoothDevice> deviceList = new ArrayList<>();
    private DeviceAdapter adapter;

    private TextView statusText;
    private ProgressBar progressBar;
    private Button btnScan;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !deviceList.contains(device)) {
                    deviceList.add(device);
                    adapter.notifyDataSetChanged();
                    updateStatus("Geräte gefunden: " + deviceList.size());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                finishScan();
            }
        }
    };

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_connect, container, false);

        statusText = view.findViewById(R.id.status_text);
        progressBar = view.findViewById(R.id.progress_bar);
        btnScan = view.findViewById(R.id.btn_scan);
        RecyclerView recyclerView = view.findViewById(R.id.customer_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DeviceAdapter();
        recyclerView.setAdapter(adapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnScan.setOnClickListener(v -> {
            if (bluetoothAdapter == null) {
                updateStatus("Bluetooth nicht unterstützt");
                return;
            }
            toggleScan();
        });

        checkPermissions();

        return view;
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            requestPermissions(permissionsNeeded.toArray(new String[0]), 1001);
        } else {
            initBluetooth();
        }
    }

    @SuppressLint("MissingPermission")
    private void initBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable(); // Oder per Intent
        } else {
            loadPairedDevices();
        }
    }

    @SuppressLint("MissingPermission")
    private void loadPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        deviceList.clear();
        deviceList.addAll(pairedDevices);
        adapter.notifyDataSetChanged();
        updateStatus("Gekoppelte Geräte geladen");
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void toggleScan() {
        if (bluetoothAdapter.isDiscovering()) {
            cancelScan();
        } else {
            startScan();
        }
    }

    @SuppressLint("MissingPermission")
    private void startScan() {
        deviceList.clear();
        adapter.notifyDataSetChanged();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        requireActivity().registerReceiver(receiver, filter);

        bluetoothAdapter.startDiscovery();
        progressBar.setVisibility(View.VISIBLE);
        btnScan.setText("Scan stoppen");
        updateStatus("Suche läuft...");

        new Handler().postDelayed(this::finishScan, SCAN_TIMEOUT);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void finishScan() {
        if (bluetoothAdapter.isDiscovering()) {
            cancelScan();
        }
        updateStatus("Scan abgeschlossen");
    }

    @SuppressLint("MissingPermission")
    private void cancelScan() {
        bluetoothAdapter.cancelDiscovery();
        progressBar.setVisibility(View.GONE);
        btnScan.setText("Scannen");
        try {
            requireActivity().unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // Receiver nicht registriert
        }
    }

    private void updateStatus(String message) {
        statusText.setText(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireActivity().unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public String getTitle() {
        return "Kunde verbinden";
    }

    private class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView deviceName, deviceAddress, deviceStatus;

            ViewHolder(View view) {
                super(view);
                deviceName = view.findViewById(R.id.device_name);
                deviceAddress = view.findViewById(R.id.device_address);
                deviceStatus = view.findViewById(R.id.device_status);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bluetooth_device, parent, false);
            return new ViewHolder(view);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BluetoothDevice device = deviceList.get(position);

            holder.deviceName.setText(device.getName() != null ? device.getName() : "Unbekannt");
            holder.deviceAddress.setText(device.getAddress());

            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                holder.deviceStatus.setText("Gekoppelt");
                holder.deviceStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.connected));
            } else {
                holder.deviceStatus.setText("Nicht gekoppelt");
                holder.deviceStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.disconnected));
            }

            holder.itemView.setOnClickListener(v -> {
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    device.createBond();
                    Toast.makeText(requireContext(), "Kopplungsanfrage gesendet an: " + device.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Gerät ist bereits gekoppelt", Toast.LENGTH_SHORT).show();
                    // Optional: Verbindung herstellen
                }
            });
        }

        @Override
        public int getItemCount() {
            return deviceList.size();
        }
    }
}
