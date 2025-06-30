package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bluetooth {
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private final List<BluetoothDevice> deviceList;
    private BluetoothSocket socket;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public interface BluetoothCallback {
        void onDeviceFound(BluetoothDevice device);
        void onDiscoveryFinished();
    }

    private BluetoothCallback callback;

    public Bluetooth(Context context, BluetoothCallback callback) {
        this.context = context;
        this.callback = callback;
        this.deviceList = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void initializeBluetooth() {
        if (bluetoothAdapter == null) {
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !deviceList.contains(device)) {
                    deviceList.add(device);
                    callback.onDeviceFound(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                callback.onDiscoveryFinished();
            }
        }
    };

    public void startDiscovery() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        deviceList.clear();
        bluetoothAdapter.startDiscovery();
    }

    public void connectToDevice(String deviceAddress) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(byte[] data) {
        if (socket != null && socket.isConnected()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<BluetoothDevice> getDeviceList() {
        return deviceList;
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanup() {
        context.unregisterReceiver(receiver);
        disconnect();
    }
}