package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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

import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;
/**
 * ConnectFragment allows the customer to scan for nearby seller devices
 * via Bluetooth, display them in a list, and pair with them if not already paired.
 * Only smartphones are shown.
 */
public class ConnectFragment extends BaseFragment {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    private static final long SCAN_TIMEOUT = 10000;

    private BluetoothAdapter bluetoothAdapter;
    private final List<BluetoothDevice> deviceList = new ArrayList<>();
    private DeviceAdapter adapter;
    private ProgressBar progressBar;
    private TextView statusText;
    private Button btnScan;
    private boolean receiverRegistered = false;

    /**
     * BroadcastReceiver to handle Bluetooth scan results and scan completion.
     */
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
                    updateStatus("Geräte gefunden: " + deviceList.size());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                finishScan();
            }
        }
    };

    /**
     * Inflates the layout and sets up views and event handlers.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_connect, container, false);

        statusText = view.findViewById(R.id.status_text);
        progressBar = view.findViewById(R.id.progress_bar);
        btnScan = view.findViewById(R.id.btn_scan);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_devices);

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

    /**
     * Checks and requests the required Bluetooth and location permissions.
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
            requestPermissions(permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        } else {
            initBluetooth();
        }
    }

    /**
     * Enables Bluetooth if it's not already enabled.
     */
    @SuppressLint("MissingPermission")
    private void initBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            loadPairedDevices();
        }
    }

    /**
     * Loads already paired devices and filters for smartphones.
     */
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
        updateStatus("Gekoppelte Geräte geladen");
    }

    /**
     * Toggles between starting and stopping a Bluetooth scan.
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
     * Starts Bluetooth device discovery.
     */
    @SuppressLint("MissingPermission")
    private void startScan() {
        deviceList.clear();
        adapter.notifyDataSetChanged();
        registerReceiverIfNeeded();

        bluetoothAdapter.startDiscovery();
        progressBar.setVisibility(View.VISIBLE);
        btnScan.setText("Scan stoppen");
        updateStatus("Suche läuft...");

        new Handler().postDelayed(this::finishScan, SCAN_TIMEOUT);
    }

    /**
     * Stops the scan after timeout or when manually stopped.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void finishScan() {
        if (bluetoothAdapter.isDiscovering()) {
            cancelScan();
        }
        updateStatus("Scan abgeschlossen");
    }

    /**
     * Cancels an active Bluetooth scan and resets the UI.
     */
    @SuppressLint("MissingPermission")
    private void cancelScan() {
        bluetoothAdapter.cancelDiscovery();
        progressBar.setVisibility(View.GONE);
        btnScan.setText("Scannen");
        unregisterReceiverIfNeeded();
    }

    /**
     * Registers the Bluetooth receiver if it hasn't been registered yet.
     */
    private void registerReceiverIfNeeded() {
        if (!receiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            requireActivity().registerReceiver(receiver, filter);
            receiverRegistered = true;
        }
    }

    /**
     * Unregisters the Bluetooth receiver if it was registered.
     */
    private void unregisterReceiverIfNeeded() {
        if (receiverRegistered) {
            try {
                requireActivity().unregisterReceiver(receiver);
            } catch (IllegalArgumentException ignored) {}
            receiverRegistered = false;
        }
    }

    /**
     * Updates the status text shown on the screen.
     */
    private void updateStatus(String message) {
        statusText.setText(message);
    }

    /**
     * Checks if a device is already in the list.
     */
    private boolean isAlreadyDiscovered(BluetoothDevice device) {
        for (BluetoothDevice d : deviceList) {
            if (d.getAddress().equals(device.getAddress())) return true;
        }
        return false;
    }

    /**
     * Returns true if the device is a smartphone.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private boolean isSmartphone(BluetoothDevice device) {
        BluetoothClass btClass = device.getBluetoothClass();
        return btClass != null &&
                btClass.getDeviceClass() == BluetoothClass.Device.PHONE_SMART;
    }

    /**
     * Unregisters receiver when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceiverIfNeeded();
    }

    /**
     * Handles the result of permission requests.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    updateStatus("Berechtigungen benötigt!");
                    return;
                }
            }
            initBluetooth();
        }
    }

    /**
     * Handles the result from the Bluetooth enable request.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            loadPairedDevices();
        } else {
            updateStatus("Bluetooth nicht aktiviert");
        }
    }

    /**
     * Provides the title of the fragment.
     *
     * @return The fragment title
     */
    @Override
    public String getTitle() {
        return "Koppeln";
    }

    /**
     * Adapter class for displaying Bluetooth devices in a RecyclerView.
     */
    private class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

        /**
         * ViewHolder for each device entry.
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
         * Binds Bluetooth device info to the view.
         */
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
                    Toast.makeText(requireContext(), device.getName() + " ist bereits gekoppelt", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return deviceList.size();
        }
    }
}