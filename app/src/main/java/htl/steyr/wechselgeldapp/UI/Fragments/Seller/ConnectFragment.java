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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

/**
 * Fragment that handles scanning for and displaying nearby Bluetooth devices
 * for seller-side Bluetooth connection in the Wechselgeld-App.
 * <p>
 * Devices are listed and optionally bonded upon selection.
 * This fragment requires appropriate Bluetooth and location permissions.
 */
public class ConnectFragment extends BaseFragment {

    /** Duration in milliseconds to perform Bluetooth scanning before timeout. */
    private static final long SCAN_TIMEOUT = 10000;

    private BluetoothAdapter bluetoothAdapter;
    private final List<BluetoothDevice> deviceList = new ArrayList<>();
    private DeviceAdapter adapter;

    private TextView statusText;
    private ProgressBar progressBar;
    private Button btnScan;

    /**
     * BroadcastReceiver to handle discovered Bluetooth devices and discovery state changes.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !deviceList.contains(device)) {
                    deviceList.add(device);
                    adapter.notifyDataSetChanged();
                    updateStatus("Devices found: " + deviceList.size());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                finishScan();
            }
        }
    };

    /**
     * Inflates the fragment layout and sets up UI and Bluetooth logic.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views.
     * @param container          The parent view the fragment UI should be attached to.
     * @param savedInstanceState Bundle containing saved state (if any).
     * @return The root View for the fragment's UI.
     */
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

    /**
     * Checks if required permissions are granted, and requests them if not.
     */
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

    /**
     * Initializes Bluetooth by enabling it or loading paired devices if already enabled.
     */
    @SuppressLint("MissingPermission")
    private void initBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        } else {
            loadPairedDevices();
        }
    }

    /**
     * Loads previously paired (bonded) Bluetooth devices and populates the device list.
     */
    @SuppressLint("MissingPermission")
    private void loadPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        deviceList.clear();
        deviceList.addAll(pairedDevices);
        adapter.notifyDataSetChanged();
        updateStatus("Paired devices loaded");
    }

    /**
     * Starts or stops a Bluetooth device discovery scan.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void toggleScan() {
        if (bluetoothAdapter.isDiscovering()) {
            cancelScan();
        } else {
            startScan();
        }
    }

    /**
     * Starts Bluetooth device discovery, registers broadcast receiver, and sets scan timeout.
     */
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
        btnScan.setText("Stop Scan");
        updateStatus("Scanning...");

        new Handler().postDelayed(this::finishScan, SCAN_TIMEOUT);
    }

    /**
     * Ends the current Bluetooth scan if still active and updates UI.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void finishScan() {
        if (bluetoothAdapter.isDiscovering()) {
            cancelScan();
        }
        updateStatus("Scan completed");
    }

    /**
     * Cancels any ongoing discovery and unregisters the broadcast receiver.
     */
    @SuppressLint("MissingPermission")
    private void cancelScan() {
        bluetoothAdapter.cancelDiscovery();
        progressBar.setVisibility(View.GONE);
        btnScan.setText("Scan");
        try {
            requireActivity().unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // Receiver was not registered
        }
    }

    /**
     * Updates the status message shown to the user.
     *
     * @param message The status message to display.
     */
    private void updateStatus(String message) {
        statusText.setText(message);
    }

    /**
     * Cleans up resources by unregistering the Bluetooth receiver when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireActivity().unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * Provides the title for this fragment, shown in the app UI.
     *
     * @return The title string for this fragment.
     */
    @Override
    public String getTitle() {
        return "Connect Customer";
    }

    /**
     * RecyclerView Adapter for displaying a list of discovered or paired Bluetooth devices.
     */
    private class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

        /**
         * ViewHolder that holds references to the views for a single Bluetooth device item.
         */
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

        /**
         * Binds a Bluetooth device to the ViewHolder and sets up interaction logic.
         *
         * @param holder   The ViewHolder instance.
         * @param position The position of the item in the dataset.
         */
        @SuppressLint("MissingPermission")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BluetoothDevice device = deviceList.get(position);

            holder.deviceName.setText(device.getName() != null ? device.getName() : "Unknown");
            holder.deviceAddress.setText(device.getAddress());

            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                holder.deviceStatus.setText("Paired");
                holder.deviceStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.connected));
            } else {
                holder.deviceStatus.setText("Not paired");
                holder.deviceStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.disconnected));
            }

            holder.itemView.setOnClickListener(v -> {
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    device.createBond();
                    Toast.makeText(requireContext(), "Pairing request sent to: " + device.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Verbinde mit " + device.getName(), Toast.LENGTH_SHORT).show();

                    BluetoothManager.getInstance(requireContext(), new Bluetooth.BluetoothCallback() {
                        @Override
                        public void onConnectionSuccess(BluetoothDevice connectedDevice) {
                            Toast.makeText(requireContext(), "Verbunden mit: " + connectedDevice.getName(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(requireContext(), "Fehler: " + error, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDataSent(boolean success) {}
                        @Override
                        public void onDeviceFound(BluetoothDevice device) {}
                        @Override
                        public void onScanFinished() {}
                        @Override
                        public void onScanStarted() {}
                        @Override
                        public void onDataReceived(htl.steyr.wechselgeldapp.Backup.UserData data) {}
                        @Override
                        public void onDisconnected() {}
                    }).connectToDevice(device);
                }
            });

        }

        /**
         * Returns the number of items in the dataset.
         *
         * @return The total number of Bluetooth devices in the list.
         */
        @Override
        public int getItemCount() {
            return deviceList.size();
        }
    }
}
