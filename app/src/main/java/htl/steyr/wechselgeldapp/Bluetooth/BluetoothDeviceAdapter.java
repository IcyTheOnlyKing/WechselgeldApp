package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import htl.steyr.wechselgeldapp.R;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder> {

    private List<BluetoothDevice> devices = new ArrayList<>();
    private OnDeviceClickListener listener;

    public BluetoothDeviceAdapter(List<BluetoothDevice> deviceList, Context context) {
        this.devices = deviceList;
        this.listener = (OnDeviceClickListener) context; // Context muss OnDeviceClickListener implementieren
    }

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }

    public BluetoothDeviceAdapter(OnDeviceClickListener listener) {
        this.listener = listener;
    }

    public void addDevice(BluetoothDevice device) {
        if (!devices.contains(device)) {
            devices.add(device);
            notifyItemInserted(devices.size() - 1);
        }
    }

    public void clearDevices() {
        devices.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_fragment_connect, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.bind(device, listener);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceName;
        private TextView deviceAddress;
        private TextView deviceStatus;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);

        }

        public void bind(BluetoothDevice device, OnDeviceClickListener listener) {
            Context context = itemView.getContext();

            // Gerätename
            String name = "Unbekanntes Gerät";
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                if (device.getName() != null && !device.getName().isEmpty()) {
                    name = device.getName();
                }
            }
            deviceName.setText(name);

            // Geräteadresse
            deviceAddress.setText(device.getAddress());

            // Kopplungsstatus
            String status = "Nicht gekoppelt";
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                status = "Gekoppelt";
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                status = "Kopplung läuft...";
            }
            deviceStatus.setText(status);

            // Click Listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeviceClick(device);
                }
            });
        }
    }
}