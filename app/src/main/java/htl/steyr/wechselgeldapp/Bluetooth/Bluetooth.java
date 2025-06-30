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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bluetooth {
    private static final String TAG = "Bluetooth";
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private final Set<BluetoothDevice> deviceSet; // Set verhindert Duplikate
    private BluetoothSocket socket;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Handler mainHandler;
    private boolean isDiscovering = false;

    public interface BluetoothCallback {
        void onDeviceFound(BluetoothDevice device);
        void onDiscoveryFinished();
        void onDiscoveryStarted();
        void onBluetoothError(String error);
    }

    private BluetoothCallback callback;

    public Bluetooth(Context context, BluetoothCallback callback) {
        this.context = context;
        this.callback = callback;
        this.deviceSet = new HashSet<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean initializeBluetooth() {
        if (bluetoothAdapter == null) {
            callback.onBluetoothError("Bluetooth nicht verfügbar");
            return false;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            callback.onBluetoothError("Bluetooth muss aktiviert werden");
            return false;
        }

        // Registriere Receiver für verschiedene Bluetooth-Events
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        try {
            context.registerReceiver(receiver, filter);
            Log.d(TAG, "BroadcastReceiver registriert");
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Registrieren des Receivers", e);
            return false;
        }

        return true;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    // Verwende Set um Duplikate zu vermeiden
                    if (deviceSet.add(device)) {
                        Log.d(TAG, "Neues Gerät gefunden: " + getDeviceName(device));
                        mainHandler.post(() -> callback.onDeviceFound(device));
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                isDiscovering = true;
                Log.d(TAG, "Discovery gestartet");
                mainHandler.post(() -> callback.onDiscoveryStarted());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                isDiscovering = false;
                Log.d(TAG, "Discovery beendet. Gefundene Geräte: " + deviceSet.size());
                mainHandler.post(() -> callback.onDiscoveryFinished());
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    mainHandler.post(() -> callback.onBluetoothError("Bluetooth wurde deaktiviert"));
                }
            }
        }
    };

    public boolean startDiscovery() {
        if (bluetoothAdapter == null) {
            callback.onBluetoothError("Bluetooth Adapter nicht verfügbar");
            return false;
        }

        // Prüfe Berechtigungen
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            callback.onBluetoothError("BLUETOOTH_SCAN Berechtigung fehlt");
            return false;
        }

        // Stoppe laufende Discovery
        if (isDiscovering) {
            Log.d(TAG, "Stoppe laufende Discovery");
            bluetoothAdapter.cancelDiscovery();
        }

        // Lösche alte Geräteliste
        deviceSet.clear();

        // Füge bereits gepaarte Geräte hinzu
        addPairedDevices();

        // Starte neue Discovery
        boolean started = bluetoothAdapter.startDiscovery();
        if (started) {
            Log.d(TAG, "Discovery erfolgreich gestartet");
        } else {
            Log.e(TAG, "Fehler beim Starten der Discovery");
            callback.onBluetoothError("Fehler beim Starten der Gerätesuche");
        }

        return started;
    }

    private void addPairedDevices() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "BLUETOOTH_CONNECT Berechtigung fehlt für gepaarte Geräte");
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            Log.d(TAG, "Füge " + pairedDevices.size() + " gepaarte Geräte hinzu");
            for (BluetoothDevice device : pairedDevices) {
                if (deviceSet.add(device)) {
                    mainHandler.post(() -> callback.onDeviceFound(device));
                }
            }
        }
    }

    public void connectToDevice(String deviceAddress) {
        if (bluetoothAdapter == null) {
            callback.onBluetoothError("Bluetooth Adapter nicht verfügbar");
            return;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                callback.onBluetoothError("BLUETOOTH_CONNECT Berechtigung fehlt");
                return;
            }

            // Stoppe Discovery für bessere Verbindungsperformance
            if (isDiscovering) {
                bluetoothAdapter.cancelDiscovery();
            }

            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
            Log.d(TAG, "Erfolgreich verbunden mit: " + getDeviceName(device));
        } catch (IOException e) {
            Log.e(TAG, "Verbindungsfehler", e);
            callback.onBluetoothError("Verbindung fehlgeschlagen: " + e.getMessage());
        }
    }

    public boolean sendData(byte[] data) {
        if (socket != null && socket.isConnected()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
                outputStream.flush();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Fehler beim Senden", e);
                callback.onBluetoothError("Fehler beim Senden der Daten");
                return false;
            }
        } else {
            callback.onBluetoothError("Keine aktive Verbindung");
            return false;
        }
    }

    public List<BluetoothDevice> getDeviceList() {
        return new ArrayList<>(deviceSet);
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public boolean isDiscovering() {
        return isDiscovering;
    }

    private String getDeviceName(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return device.getAddress();
        }
        String name = device.getName();
        return (name != null && !name.isEmpty()) ? name : "Unbekanntes Gerät (" + device.getAddress() + ")";
    }

    public void stopDiscovery() {
        if (bluetoothAdapter != null && isDiscovering) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "Discovery gestoppt");
            }
        }
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
                Log.d(TAG, "Socket geschlossen");
            }
        } catch (IOException e) {
            Log.e(TAG, "Fehler beim Schließen des Sockets", e);
        }
    }

    public void cleanup() {
        try {
            stopDiscovery();
            disconnect();
            context.unregisterReceiver(receiver);
            Log.d(TAG, "Cleanup abgeschlossen");
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Cleanup", e);
        }
    }
}