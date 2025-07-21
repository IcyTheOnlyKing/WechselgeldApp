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

/**
 * RecyclerView adapter for displaying a list of Bluetooth devices.
 * Used for selecting devices to connect to in the customer UI.
 */
public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    /**
     * Listener interface for handling clicks on device list items.
     */
    public interface OnDeviceClickListener {
        /**
         * Called when a Bluetooth device in the list is clicked.
         *
         * @param device The clicked Bluetooth device.
         */
        void onDeviceClick(BluetoothDevice device);
    }

    private final List<BluetoothDevice> deviceList;
    private final OnDeviceClickListener listener;

    /**
     * Constructor for the BluetoothDeviceAdapter.
     *
     * @param deviceList List of discovered Bluetooth devices to display.
     * @param listener Callback to handle clicks on list items.
     */
    public BluetoothDeviceAdapter(List<BluetoothDevice> deviceList, OnDeviceClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    /**
     * Inflates the layout for a single device item view.
     *
     * @param parent The parent view group.
     * @param viewType View type (unused here).
     * @return A new ViewHolder for the list item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_fragment_connect, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds device data to the view holder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the device in the list.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);
        holder.deviceName.setText(device.getName() != null ? device.getName() : "Unknown device");
        holder.deviceAddress.setText(device.getAddress());

        holder.itemView.setOnClickListener(v -> listener.onDeviceClick(device));
    }

    /**
     * Returns the total number of devices in the list.
     *
     * @return Item count.
     */
    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    /**
     * ViewHolder class for representing a single Bluetooth device item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;

        /**
         * Initializes the ViewHolder by referencing the TextViews for name and address.
         *
         * @param itemView The inflated layout view.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
        }
    }
}
