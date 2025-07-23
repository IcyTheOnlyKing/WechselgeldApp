package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import htl.steyr.wechselgeldapp.Backup.UserData;
import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
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
    private boolean receiverRegistered = false;
    private boolean isConnecting = false;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !isAlreadyDiscovered(device) && isSmartphone(device)) {
                    deviceList.add(device);
                    adapter.notifyDataSetChanged();
                    updateStatus("Devices found: " + deviceList.size());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                finishScan();
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING && device != null) {
                    Toast.makeText(context, "Pairing complete, connecting to: " + device.getName(), Toast.LENGTH_SHORT).show();
                    connectToDevice(device);
                }
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
                updateStatus("Bluetooth not supported");
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
            boolean success = bluetoothAdapter.enable();
            if (!success) {
                updateStatus("Failed to enable Bluetooth.");
                return;
            }
        }
        loadPairedDevices();
    }

    @SuppressLint("MissingPermission")
    private void loadPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        deviceList.clear();
        for (BluetoothDevice device : pairedDevices) {
            if (isSmartphone(device)) {
                deviceList.add(device);
            }
        }
        adapter.notifyDataSetChanged();
        updateStatus("Paired devices loaded");
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
        registerReceiverIfNeeded();

        bluetoothAdapter.startDiscovery();
        progressBar.setVisibility(View.VISIBLE);
        btnScan.setText(R.string.stop_scan); // Use string resource
        updateStatus(getString(R.string.scanning));

        new Handler().postDelayed(this::finishScan, SCAN_TIMEOUT);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void finishScan() {
        if (bluetoothAdapter.isDiscovering()) {
            cancelScan();
        }
        updateStatus(getString(R.string.scan_complete));
    }

    @SuppressLint("MissingPermission")
    private void cancelScan() {
        bluetoothAdapter.cancelDiscovery();
        progressBar.setVisibility(View.GONE);
        btnScan.setText(R.string.scan);
        unregisterReceiverIfNeeded();
    }

    private void registerReceiverIfNeeded() {
        if (!receiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            requireActivity().registerReceiver(receiver, filter);
            receiverRegistered = true;
        }
    }

    private void unregisterReceiverIfNeeded() {
        if (receiverRegistered) {
            try {
                requireActivity().unregisterReceiver(receiver);
            } catch (IllegalArgumentException ignored) {}
            receiverRegistered = false;
        }
    }

    private void updateStatus(String message) {
        statusText.setText(message);
    }

    private boolean isAlreadyDiscovered(BluetoothDevice device) {
        for (BluetoothDevice d : deviceList) {
            if (d.getAddress().equals(device.getAddress())) return true;
        }
        return false;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private boolean isSmartphone(BluetoothDevice device) {
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        return bluetoothClass != null &&
                bluetoothClass.getDeviceClass() == BluetoothClass.Device.PHONE_SMART;
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void connectToDevice(BluetoothDevice device) {
        if (isConnecting) return;
        isConnecting = true;

        BluetoothManager.getInstance(requireContext(), new Bluetooth.BluetoothCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onConnectionSuccess(BluetoothDevice connectedDevice) {
                isConnecting = false;
                Toast.makeText(requireContext(), "Connected to: " + connectedDevice.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                isConnecting = false;
                Toast.makeText(requireContext(), "Connection error: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override public void onDataSent(boolean success) {}
            @Override public void onDataReceived(UserData data) {}

            @Override
            public void onDataReceivedRaw(String message) {

            }

            @Override public void onDisconnected() {}
            @Override public void onScanStarted() {}
            @Override public void onScanFinished() {}
            @Override public void onDeviceFound(BluetoothDevice device) {}
        }).connectToDevice(device);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceiverIfNeeded();
    }

    @Override
    public String getTitle() {
        return "Connect Customer";
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

            holder.deviceName.setText(device.getName() != null ? device.getName() : "Unknown");
            holder.deviceAddress.setText(device.getAddress());

            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                holder.deviceStatus.setText(R.string.paired);
                holder.deviceStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.connected));
            } else {
                holder.deviceStatus.setText(R.string.not_paired);
                holder.deviceStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.disconnected));
            }

            holder.itemView.setOnClickListener(v -> {
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    device.createBond();
                } else {
                    connectToDevice(device);
                }
            });
        }

        @Override
        public int getItemCount() {
            return deviceList.size();
        }
    }
}
