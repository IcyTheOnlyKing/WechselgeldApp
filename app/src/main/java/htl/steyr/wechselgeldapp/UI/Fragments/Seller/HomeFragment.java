package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import htl.steyr.wechselgeldapp.Bluetooth.BluetoothDeviceAdapter;
import htl.steyr.wechselgeldapp.R;

public class HomeFragment extends Fragment {
    private RecyclerView deviceListRecyclerView;
    private BluetoothDeviceAdapter bluetoothDeviceAdapter;
    private List<BluetoothDevice> deviceList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_home, container, false);

        // Initialize RecyclerView
        deviceListRecyclerView = view.findViewById(R.id.device_list);
        deviceListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize device list and adapter
        deviceList = new ArrayList<>();
        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(deviceList, getContext());
        deviceListRecyclerView.setAdapter(bluetoothDeviceAdapter);

        // Load bluetooth devices
        loadBluetoothDevices();

        return view;
    }

    private void loadBluetoothDevices() {
        // Implementierung zum Laden der Bluetooth-Geräte
        // Diese Methode sollte die deviceList füllen und den Adapter benachrichtigen
        bluetoothDeviceAdapter.notifyDataSetChanged();
    }
}