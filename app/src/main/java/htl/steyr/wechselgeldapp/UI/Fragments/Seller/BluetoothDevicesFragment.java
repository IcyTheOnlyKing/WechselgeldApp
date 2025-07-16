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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.Backup.UserData;
import htl.steyr.wechselgeldapp.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDevicesFragment extends Fragment implements Bluetooth.BluetoothCallback {
    private Bluetooth bluetooth;
    private RecyclerView customerList;
    private BluetoothCustomerAdapter customerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final List<BluetoothDevice> nearbyDevices = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_history, container, false);

        customerList = view.findViewById(R.id.customer_list);

        // SwipeRefreshLayout hinzufügen für manuellen Scan
        setupSwipeRefresh(view);
        setupRecyclerView();
        setupBluetooth();

        // Automatischen Scan beim Laden starten
        startBluetoothScan();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void setupSwipeRefresh(View view) {
        // SwipeRefreshLayout wird das CardView umschließen
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::startBluetoothScan);
        }
    }

    private void setupRecyclerView() {
        customerAdapter = new BluetoothCustomerAdapter(nearbyDevices, this::onCustomerSelected);
        customerList.setLayoutManager(new LinearLayoutManager(getContext()));
        customerList.setAdapter(customerAdapter);
    }

    private void setupBluetooth() {
        bluetooth = new Bluetooth(getContext(), this);
        BluetoothManager.setInstance(bluetooth);
        if (!bluetooth.init()) {
            Toast.makeText(getContext(), "Bluetooth-Initialisierung fehlgeschlagen", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void startBluetoothScan() {
        if (checkPermissions()) {
            nearbyDevices.clear();
            customerAdapter.notifyDataSetChanged();

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

    private void onCustomerSelected(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            bluetooth.connectToDevice(device);
        }
    }

    // Bluetooth Callback Implementierungen
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        if (!nearbyDevices.contains(device)) {
            nearbyDevices.add(device);
            customerAdapter.notifyItemInserted(nearbyDevices.size() - 1);
        }
    }

    @Override
    public void onScanStarted() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        nearbyDevices.clear();
        customerAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Suche nach Kunden gestartet...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScanFinished() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        Toast.makeText(getContext(), nearbyDevices.size() + " Kunden in der Nähe gefunden", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String error) {
        Toast.makeText(getContext(), "Fehler: " + error, Toast.LENGTH_SHORT).show();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onConnectionSuccess(BluetoothDevice device) {
        String deviceName = "Unbekannter Kunde";
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            if (device.getName() != null) deviceName = device.getName();
            else deviceName = "Unbekannter Kunde";
        }
        BluetoothManager.setInstance(bluetooth);
        Toast.makeText(getContext(), "Verbunden mit Kunde: " + deviceName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataSent(boolean success) {
        String message = success ? "Daten erfolgreich an Kunde gesendet" : "Datenübertragung an Kunde fehlgeschlagen";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataReceived(UserData data) {
        // not used here
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(getContext(), "Verbindung zu Kunde getrennt", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onResume() {
        super.onResume();
        // Automatisch neu scannen wenn Fragment wieder sichtbar wird
        startBluetoothScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetooth != null) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetooth.cleanup();
            }
        }
    }

    // RecyclerView Adapter für Bluetooth-Kunden
    private static class BluetoothCustomerAdapter extends RecyclerView.Adapter<BluetoothCustomerAdapter.CustomerViewHolder> {
        private final List<BluetoothDevice> customers;
        private final OnCustomerClickListener clickListener;

        public interface OnCustomerClickListener {
            void onCustomerClick(BluetoothDevice device);
        }

        public BluetoothCustomerAdapter(List<BluetoothDevice> customers, OnCustomerClickListener clickListener) {
            this.customers = customers;
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
            BluetoothDevice device = customers.get(position);

            if (ActivityCompat.checkSelfPermission(holder.itemView.getContext(),
                    Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {

                String customerName;
                if (device.getName() != null) customerName = device.getName();
                else customerName = "Unbekannter Kunde";
                String deviceAddress = device.getAddress();

                holder.customerNameTextView.setText(customerName);
                holder.deviceAddressTextView.setText(deviceAddress);
                holder.statusTextView.setText("In der Nähe");

                holder.itemView.setOnClickListener(v -> clickListener.onCustomerClick(device));
            }
        }

        @Override
        public int getItemCount() {
            return customers.size();
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