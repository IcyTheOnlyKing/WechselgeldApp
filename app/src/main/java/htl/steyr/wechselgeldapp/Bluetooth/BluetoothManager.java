package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

/**
 * Singleton manager for handling a single global instance of the Bluetooth class.
 */
public class BluetoothManager {

    @SuppressLint("StaticFieldLeak")
    private static Bluetooth instance;

    private BluetoothManager() {}

    /**
     * Initializes or updates the global Bluetooth instance with context and callback.
     *
     * @param context  Context to initialize Bluetooth (must be application context-safe).
     * @param callback Callback for Bluetooth events.
     * @return Global Bluetooth instance.
     */
    public static synchronized Bluetooth getInstance(Context context, @Nullable Bluetooth.BluetoothCallback callback) {
        if (instance == null) {
            instance = new Bluetooth(context.getApplicationContext(), callback);
        } else if (callback != null) {
            instance.setCallback(callback); // Update callback for new fragment or context
        }
        return instance;
    }

    /**
     * Returns the current Bluetooth instance.
     * @return Bluetooth singleton instance.
     * @throws IllegalStateException if not yet initialized via getInstance(context, callback).
     */
    public static Bluetooth getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BluetoothManager not initialized. Call getInstance(context, callback) first.");
        }
        return instance;
    }

    /**
     * Destroys the current Bluetooth instance and unregisters all receivers.
     * @requiresPermission android.Manifest.permission.BLUETOOTH_SCAN
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public static synchronized void destroyInstance() {
        if (instance != null) {
            instance.cleanup();
            instance = null;
        }
    }

    /**
     * Checks if the Bluetooth instance has been initialized.
     * @return true if initialized, false otherwise.
     */
    public static boolean isInitialized() {
        return instance != null;
    }
}
