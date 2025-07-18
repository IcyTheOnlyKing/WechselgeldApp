package htl.steyr.wechselgeldapp.UI.Fragments.Seller;


import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.Backup.UserData;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

/**
 * Fragment used by the seller device to search for and connect to a customer
 * phone. It mirrors the basic behaviour of {@link htl.steyr.wechselgeldapp.UI.Fragments.Customer.ConnectFragment}.
 */
public class ConnectFragment extends BaseFragment implements Bluetooth.BluetoothCallback {

    private Bluetooth bluetooth;
    private RecyclerView deviceList;
    private BluetoothCustomerAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final List<BluetoothDevice> nearbyDevices = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_connect, container, false);

        deviceList = view.findViewById(R.id.customer_list);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        adapter = new BluetoothCustomerAdapter(nearbyDevices, this::onDeviceSelected);
        deviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceList.setAdapter(adapter);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::startBluetoothScan);
        }

        bluetooth = new Bluetooth(getContext(), this);
        BluetoothManager.setInstance(bluetooth);
        if (bluetooth.init()) {
            startBluetoothScan();
        } else {
            Toast.makeText(getContext(), "Bluetooth nicht verfügbar", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void startBluetoothScan() {
        if (checkPermissions()) {
            nearbyDevices.clear();
            adapter.notifyDataSetChanged();

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetooth.startScan();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private boolean checkPermissions() {
        String[] permissions = {
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 1);
                return false;
            }
        }
        return true;
    }

    private void onDeviceSelected(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            bluetooth.connectToDevice(device);
        }
    }

    // --- Bluetooth callbacks ---
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        if (!nearbyDevices.contains(device)) {
            nearbyDevices.add(device);
            adapter.notifyItemInserted(nearbyDevices.size() - 1);
        }
    }

    @Override
    public void onScanStarted() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        nearbyDevices.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onScanFinished() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getContext(), nearbyDevices.size() + " Geräte gefunden", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String error) {
        Toast.makeText(getContext(), "Fehler: " + error, Toast.LENGTH_SHORT).show();
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onConnectionSuccess(BluetoothDevice device) {
        BluetoothManager.setInstance(bluetooth);
        String name;
        if (device.getName() != null) name = device.getName();
        else name = "Unbekannt";
        Toast.makeText(getContext(), "Verbunden mit " + name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataSent(boolean success) {
        // Not used here
    }

    @Override
    public void onDataReceived(UserData data) {
        // Seller does not expect to receive data
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(getContext(), "Verbindung getrennt", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onResume() {
        super.onResume();
        startBluetoothScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetooth != null && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetooth.cleanup();
        }
    }

    @Override
    public String getTitle() {
        return "Koppeln";
    }

    // --- RecyclerView adapter for Bluetooth devices ---
    private static class BluetoothCustomerAdapter extends RecyclerView.Adapter<BluetoothCustomerAdapter.CustomerViewHolder> {
        private final List<BluetoothDevice> devices;
        private final OnCustomerClickListener clickListener;

        interface OnCustomerClickListener {
            void onCustomerClick(BluetoothDevice device);
        }

        BluetoothCustomerAdapter(List<BluetoothDevice> devices, OnCustomerClickListener clickListener) {
            this.devices = devices;
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.customer_fragment_home, parent, false);
            return new CustomerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
            BluetoothDevice device = devices.get(position);

            if (ActivityCompat.checkSelfPermission(holder.itemView.getContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                String customerName = device.getName() != null ? device.getName() : "Unbekannt";
                holder.customerNameTextView.setText(customerName);
                holder.deviceAddressTextView.setText(device.getAddress());
                holder.statusTextView.setText("In der Nähe");
                holder.itemView.setOnClickListener(v -> clickListener.onCustomerClick(device));
            }
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        static class CustomerViewHolder extends RecyclerView.ViewHolder {
            TextView customerNameTextView;
            TextView deviceAddressTextView;
            TextView statusTextView;

            CustomerViewHolder(@NonNull View itemView) {
                super(itemView);
                customerNameTextView = itemView.findViewById(R.id.tvCustomerName);
                deviceAddressTextView = itemView.findViewById(R.id.device_address);
                statusTextView = itemView.findViewById(R.id.status_text);
            }
        }
    }
}
