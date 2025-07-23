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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import htl.steyr.wechselgeldapp.Backup.UserData;
import htl.steyr.wechselgeldapp.Database.DatabaseHelper;

public class Bluetooth {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "Bluetooth";

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
    private volatile boolean connected = false;

    public interface BluetoothCallback {
        void onDeviceFound(BluetoothDevice device);
        void onScanFinished();
        void onScanStarted();
        void onError(String error);
        void onConnectionSuccess(BluetoothDevice device);
        void onDataSent(boolean success);
        void onDataReceived(UserData data);
        void onDataReceivedRaw(String message);
        void onDisconnected();
    }

    public Bluetooth(Context context, BluetoothCallback callback) {
        this.context = context;
        this.callback = callback;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setCallback(BluetoothCallback callback) {
        this.callback = callback;
    }

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
                handler.post(callback::onScanStarted);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanning = false;
                handler.post(callback::onScanFinished);
            }
        }
    };

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private boolean isPhoneDevice(BluetoothDevice device) {
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (bluetoothClass == null) return false;
        int major = bluetoothClass.getMajorDeviceClass();
        int type = bluetoothClass.getDeviceClass();
        return major == BluetoothClass.Device.Major.PHONE ||
                type == BluetoothClass.Device.PHONE_CELLULAR ||
                type == BluetoothClass.Device.PHONE_SMART ||
                type == BluetoothClass.Device.PHONE_UNCATEGORIZED;
    }

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
            if (callback != null) callback.onError("Connection permission denied");
            return false;
        }

        new Thread(() -> {
            try {
                if (scanning) adapter.cancelDiscovery();
                if (socket != null && socket.isConnected()) socket.close();

                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                connected = true;
                Log.d(TAG, "Connected to " + device.getName());

                saveConnectedDevice(device);

                handler.post(() -> {
                    if (callback != null) callback.onConnectionSuccess(device);
                });
                startReadThread();
            } catch (IOException e) {
                connected = false;
                Log.e(TAG, "Connection failed", e);
                handler.post(() -> {
                    if (callback != null) callback.onError("Connection failed: " + e.getMessage());
                });
            }
        }).start();

        return true;
    }

    public boolean sendRawMessage(String message) {
        if (!connected || socket == null) {
            handler.post(() -> {
                if (callback != null) callback.onDataSent(false);
            });
            return false;
        }

        new Thread(() -> {
            try {
                OutputStream os = socket.getOutputStream();
                os.write((message + "\n").getBytes());
                os.flush();
                Log.d(TAG, "Sent raw: " + message);
                handler.post(() -> {
                    if (callback != null) callback.onDataSent(true);
                });
            } catch (IOException e) {
                Log.e(TAG, "Send failed", e);
                handler.post(() -> {
                    if (callback != null) {
                        callback.onError("Send failed: " + e.getMessage());
                        callback.onDataSent(false);
                    }
                });
            }
        }).start();
        return true;
    }

    public boolean sendUserData(UserData userData) {
        if (!connected || socket == null) {
            if (callback != null) callback.onError("No active connection");
            return false;
        }

        new Thread(() -> {
            try {
                String json = gson.toJson(userData);
                OutputStream os = socket.getOutputStream();
                os.write(json.getBytes());
                os.flush();
                handler.post(() -> {
                    if (callback != null) callback.onDataSent(true);
                });
                Log.d(TAG, "Sent JSON: " + json);
            } catch (IOException e) {
                Log.e(TAG, "Send JSON failed", e);
                handler.post(() -> {
                    if (callback != null) {
                        callback.onDataSent(false);
                        callback.onError("Data transmission failed: " + e.getMessage());
                    }
                });
            }
        }).start();
        return true;
    }

    public void disconnect() {
        try {
            if (socket != null && connected) {
                socket.close();
                connected = false;
                handler.post(() -> {
                    if (callback != null) callback.onDisconnected();
                });
            }
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}

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
        } catch (Exception ignored) {}
        callback = null;
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT})
    public void startServer() {
        if (adapter == null) return;

        acceptThread = new Thread(() -> {
            try {
                serverSocket = adapter.listenUsingRfcommWithServiceRecord("WechselgeldApp", MY_UUID);
                socket = serverSocket.accept();
                connected = true;
                BluetoothDevice device = socket.getRemoteDevice();
                Log.d(TAG, "Server accepted connection from " + device.getName());

                saveConnectedDevice(device);

                handler.post(() -> {
                    if (callback != null) callback.onConnectionSuccess(device);
                });
                startReadThread();
            } catch (IOException e) {
                handler.post(() -> {
                    if (callback != null) callback.onError("Server error: " + e.getMessage());
                });
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
                    String message = new String(buffer, 0, bytes).trim();

                    if (message.startsWith("amount:")) {
                        handler.post(() -> {
                            if (callback != null) callback.onDataReceivedRaw(message);
                        });
                    } else {
                        try {
                            UserData data = gson.fromJson(message, UserData.class);
                            handler.post(() -> {
                                if (callback != null) callback.onDataReceived(data);
                            });
                        } catch (JsonSyntaxException e) {
                            handler.post(() -> {
                                if (callback != null) callback.onError("Invalid data format received");
                            });
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Read failed", e);
                handler.post(() -> {
                    if (callback != null) callback.onError("Read error: " + e.getMessage());
                });
            }
        });
        readThread.start();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void saveConnectedDevice(BluetoothDevice device) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String role = prefs.getString("user_role", "");

        if (userId == -1 || role.isEmpty()) return;

        String mac = device.getAddress();
        String name = device.getName();

        DatabaseHelper db = new DatabaseHelper(context);
        if (role.equals("customer")) {
            db.insertDevice(mac, userId, null, name);
        } else if (role.equals("seller")) {
            db.insertDevice(mac, null, userId, name);
        }
    }

    public BluetoothSocket getConnectedSocket() {
        return socket;
    }
}
