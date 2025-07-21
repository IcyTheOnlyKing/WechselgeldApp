package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import htl.steyr.wechselgeldapp.Backup.UserData;

/**
 * Bluetooth communication manager for handling discovery, pairing, connection,
 * and data exchange between devices in the Wechselgeld app.
 */
public class Bluetooth {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter adapter;
    private final Context context;
    private BluetoothCallback callback;
    private final Set<BluetoothDevice> devices = new HashSet<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    private BluetoothSocket socket;
    private BluetoothServerSocket serverSocket;
    private Thread acceptThread;
    private Thread readThread;
    private boolean scanning = false;
    private boolean connected = false;

    /**
     * Sets the callback interface for Bluetooth events.
     * @param callback BluetoothCallback implementation
     */
    public void setCallback(BluetoothCallback callback) {
        this.callback = callback;
    }

    /**
     * Sends a raw string message over the established Bluetooth connection.
     * @param message the message to send
     * @return true if sending started successfully, false otherwise
     */
    public boolean sendRawMessage(String message) {
        if (!connected || socket == null) return false;
        new Thread(() -> {
            try {
                OutputStream os = socket.getOutputStream();
                os.write((message + "\n").getBytes());
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return true;
    }

    /**
     * Returns the active Bluetooth socket if connected.
     * @return the BluetoothSocket or null
     */
    public BluetoothSocket getConnectedSocket() {
        return socket;
    }

    /**
     * Interface for notifying Bluetooth-related events.
     */
    public interface BluetoothCallback {
        void onDeviceFound(BluetoothDevice device);
        void onScanFinished();
        void onScanStarted();
        void onError(String error);
        void onConnectionSuccess(BluetoothDevice device);
        void onDataSent(boolean success);
        void onDataReceived(UserData data);
        void onDisconnected();
    }

    /**
     * Creates a new Bluetooth manager instance.
     * @param context application context
     * @param callback callback for Bluetooth events
     */
    public Bluetooth(Context context, BluetoothCallback callback) {
        this.context = context;
        this.callback = callback;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Initializes the Bluetooth manager and registers receivers.
     * @return true if initialization is successful, false otherwise
     */
    public boolean init() {
        if (adapter == null || !adapter.isEnabled()) {
            if (callback != null) callback.onError("Bluetooth not available or disabled");
            return false;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        try {
            context.registerReceiver(receiver, filter);
            return true;
        } catch (Exception e) {
            if (callback != null) callback.onError("Receiver registration failed");
            return false;
        }
    }

    /**
     * Receiver for Bluetooth scan events and discovered devices.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        public void onReceive(Context context, Intent intent) {
            if (callback == null) return;

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && isPhoneDevice(device) && devices.add(device)) {
                    handler.post(() -> callback.onDeviceFound(device));
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                scanning = true;
                handler.post(() -> callback.onScanStarted());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanning = false;
                handler.post(() -> callback.onScanFinished());
            }
        }
    };

    /**
     * Checks whether the given Bluetooth device is a phone-type device.
     * @param device the Bluetooth device
     * @return true if the device is a phone, false otherwise
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private boolean isPhoneDevice(BluetoothDevice device) {
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (bluetoothClass == null) return false;
        int majorDeviceClass = bluetoothClass.getMajorDeviceClass();
        int deviceClass = bluetoothClass.getDeviceClass();
        return majorDeviceClass == BluetoothClass.Device.Major.PHONE ||
                deviceClass == BluetoothClass.Device.PHONE_CELLULAR ||
                deviceClass == BluetoothClass.Device.PHONE_SMART ||
                deviceClass == BluetoothClass.Device.PHONE_UNCATEGORIZED;
    }

    /**
     * Starts Bluetooth device discovery.
     * @return true if discovery started successfully, false otherwise
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    public boolean startScan() {
        if (adapter == null || !hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            if (callback != null) callback.onError("Scan permission denied");
            return false;
        }

        if (scanning) adapter.cancelDiscovery();
        devices.clear();
        addPairedDevices();
        return adapter.startDiscovery();
    }

    /**
     * Adds already paired (bonded) devices to the internal list if they are phones.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void addPairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return;
        Set<BluetoothDevice> paired = adapter.getBondedDevices();
        if (paired != null) {
            for (BluetoothDevice device : paired) {
                if (isPhoneDevice(device) && devices.add(device)) {
                    handler.post(() -> callback.onDeviceFound(device));
                }
            }
        }
    }

    /**
     * Stops Bluetooth device discovery.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public void stopScan() {
        if (adapter != null && scanning && hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            adapter.cancelDiscovery();
        }
    }

    /**
     * Connects to a specific Bluetooth device.
     * @param device the target Bluetooth device
     * @return true if connection process started, false otherwise
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN})
    public boolean connectToDevice(BluetoothDevice device) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            if (callback != null) callback.onError("Connection permission denied");
            return false;
        }

        new Thread(() -> {
            try {
                if (scanning) adapter.cancelDiscovery();
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                connected = true;
                handler.post(() -> callback.onConnectionSuccess(device));
                startReadThread();
            } catch (IOException e) {
                connected = false;
                handler.post(() -> callback.onError("Connection failed: " + e.getMessage()));
            }
        }).start();

        return true;
    }

    /**
     * Sends user data as a JSON string over the established Bluetooth connection.
     * @param userData the user data to send
     * @return true if sending started, false otherwise
     */
    public boolean sendUserData(UserData userData) {
        if (!connected || socket == null) {
            if (callback != null) callback.onError("No active connection");
            return false;
        }

        new Thread(() -> {
            try {
                String jsonData = gson.toJson(userData);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(jsonData.getBytes());
                outputStream.flush();
                handler.post(() -> callback.onDataSent(true));
            } catch (IOException e) {
                handler.post(() -> {
                    callback.onDataSent(false);
                    callback.onError("Data transmission failed: " + e.getMessage());
                });
            }
        }).start();

        return true;
    }

    /**
     * Disconnects any active Bluetooth connection and stops reading/accepting threads.
     */
    public void disconnect() {
        try {
            if (socket != null && connected) {
                socket.close();
                connected = false;
                if (callback != null) callback.onDisconnected();
            }
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}

        if (readThread != null) readThread.interrupt();
        if (acceptThread != null) acceptThread.interrupt();
    }

    /**
     * Checks if a Bluetooth connection is currently active.
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected && socket != null && socket.isConnected();
    }

    /**
     * Checks if Bluetooth is enabled on the device.
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return adapter != null && adapter.isEnabled();
    }

    /**
     * Checks whether a specific permission is granted.
     * @param permission the permission to check
     * @return true if granted, false otherwise
     */
    private boolean hasPermission(String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Cleans up Bluetooth resources, stops scanning and disconnects if necessary.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public void cleanup() {
        try {
            disconnect();
            stopScan();
            context.unregisterReceiver(receiver);
        } catch (Exception ignored) {}

        callback = null;
    }

    /**
     * Starts the Bluetooth server to listen for incoming connections.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT})
    public void startServer() {
        if (adapter == null) return;

        acceptThread = new Thread(() -> {
            try {
                serverSocket = adapter.listenUsingRfcommWithServiceRecord("WechselgeldApp", MY_UUID);
                socket = serverSocket.accept();
                connected = true;
                handler.post(() -> callback.onConnectionSuccess(socket.getRemoteDevice()));
                startReadThread();
            } catch (IOException e) {
                handler.post(() -> callback.onError("Server error: " + e.getMessage()));
            }
        });
        acceptThread.start();
    }

    /**
     * Starts a thread to continuously read incoming data from the connected socket.
     * The received data is assumed to be JSON-formatted and deserialized into a UserData object.
     */
    private void startReadThread() {
        if (socket == null) return;

        readThread = new Thread(() -> {
            try {
                InputStream is = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;
                while ((bytes = is.read(buffer)) != -1) {
                    String json = new String(buffer, 0, bytes);
                    UserData data = gson.fromJson(json, UserData.class);
                    handler.post(() -> callback.onDataReceived(data));
                }
            } catch (IOException e) {
                handler.post(() -> callback.onError("Read error: " + e.getMessage()));
            }
        });
        readThread.start();
    }
}
