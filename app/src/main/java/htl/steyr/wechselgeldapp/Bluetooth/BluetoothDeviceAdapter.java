package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import htl.steyr.wechselgeldapp.R;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    public BluetoothDeviceAdapter(List<BluetoothDevice> deviceList, Context context) {
        this.devices = deviceList;
        this.listener = (OnDeviceClickListener) context; // Context muss OnDeviceClickListener implementieren
    }

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }

    private final List<BluetoothDevice> deviceList;
    private final OnDeviceClickListener listener;

    public BluetoothDeviceAdapter(List<BluetoothDevice> deviceList, OnDeviceClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_fragment_connect, parent, false);
        return new ViewHolder(view);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);
        holder.deviceName.setText(device.getName() != null ? device.getName() : "Unbekanntes Gerät");
        holder.deviceAddress.setText(device.getAddress());

        holder.itemView.setOnClickListener(v -> listener.onDeviceClick(device));
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
        }
    }
}