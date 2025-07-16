package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothServerSocket;
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

import htl.steyr.wechselgeldapp.Backup.UserData;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Bluetooth {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter adapter;
    private final Context context;
    private final Set<BluetoothDevice> devices = new HashSet<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final BluetoothCallback callback;
    private final Gson gson = new Gson();

    private BluetoothSocket socket;
    private BluetoothServerSocket serverSocket;
    private Thread acceptThread;
    private Thread readThread;
    private boolean scanning = false;
    private boolean connected = false;

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

    public Bluetooth(Context context, BluetoothCallback callback) {
        this.context = context;
        this.callback = callback;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean init() {
        if (adapter == null || !adapter.isEnabled()) {
            callback.onError("Bluetooth nicht verfügbar oder deaktiviert");
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
            callback.onError("Receiver-Registrierung fehlgeschlagen");
            return false;
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        public void onReceive(Context context, Intent intent) {
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

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    public boolean startScan() {
        if (adapter == null || !hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            callback.onError("Keine Scan-Berechtigung");
            return false;
        }

        if (scanning) adapter.cancelDiscovery();

        devices.clear();
        addPairedDevices();

        return adapter.startDiscovery();
    }

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

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public void stopScan() {
        if (adapter != null && scanning && hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            adapter.cancelDiscovery();
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN})
    public boolean connectToDevice(BluetoothDevice device) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            callback.onError("Keine Verbindungsberechtigung");
            return false;
        }

        new Thread(() -> {
            try {
                if (scanning) {
                    adapter.cancelDiscovery();
                }

                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                connected = true;

                handler.post(() -> callback.onConnectionSuccess(device));
                startReadThread();
            } catch (IOException e) {
                connected = false;
                handler.post(() -> callback.onError("Verbindung fehlgeschlagen: " + e.getMessage()));
            }
        }).start();

        return true;
    }

    public boolean sendUserData(UserData userData) {
        if (!connected || socket == null) {
            callback.onError("Keine aktive Verbindung");
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
                    callback.onError("Datenübertragung fehlgeschlagen: " + e.getMessage());
                });
            }
        }).start();

        return true;
    }

    public void disconnect() {
        if (socket != null && connected) {
            try {
                socket.close();
                connected = false;
                handler.post(() -> callback.onDisconnected());
            } catch (IOException e) {
                callback.onError("Trennung fehlgeschlagen: " + e.getMessage());
            }
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) { }
        }
        if (readThread != null) readThread.interrupt();
        if (acceptThread != null) acceptThread.interrupt();
    }

    public boolean isConnected() {
        return connected && socket != null && socket.isConnected();
    }

    public boolean isEnabled() {
        return adapter != null && adapter.isEnabled();
    }

    private boolean hasPermission(String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public void cleanup() {
        try {
            disconnect();
            stopScan();
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ACCEPT)
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
                handler.post(() -> callback.onError("Serverfehler: " + e.getMessage()));
            }
        });
        acceptThread.start();
    }

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
                handler.post(() -> callback.onError("Lesefehler: " + e.getMessage()));
            }
        });
        readThread.start();
    }
}