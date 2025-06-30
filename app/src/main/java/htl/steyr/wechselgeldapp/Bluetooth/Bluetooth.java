package htl.steyr.wechselgeldapp.Bluetooth;

    import android.Manifest;
    import android.bluetooth.BluetoothAdapter;
    import android.bluetooth.BluetoothClass;
    import android.bluetooth.BluetoothDevice;
    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.pm.PackageManager;
    import android.os.Handler;
    import android.os.Looper;

    import androidx.annotation.RequiresPermission;
    import androidx.core.app.ActivityCompat;
    import java.util.HashSet;
    import java.util.Set;

    public class Bluetooth {
        private final BluetoothAdapter adapter;
        private final Context context;
        private final Set<BluetoothDevice> devices = new HashSet<>();
        private final Handler handler = new Handler(Looper.getMainLooper());
        private BluetoothCallback callback;
        private boolean scanning = false;

        public interface BluetoothCallback {
            void onDeviceFound(BluetoothDevice device);
            void onScanFinished();
            void onScanStarted();
            void onError(String error);
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

            // Handy/Smartphone-Klassen
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

        public boolean isEnabled() {
            return adapter != null && adapter.isEnabled();
        }

        private boolean hasPermission(String permission) {
            return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        public void cleanup() {
            try {
                stopScan();
                context.unregisterReceiver(receiver);
            } catch (Exception e) {
            }
        }
    }