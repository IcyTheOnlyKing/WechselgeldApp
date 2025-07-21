package htl.steyr.wechselgeldapp.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.RequiresPermission;

/**
 * Singleton manager class for providing and managing a single global instance of the Bluetooth class.
 * Ensures consistent use of Bluetooth functionality across the app.
 */
public class BluetoothManager {
    @SuppressLint("StaticFieldLeak")
    private static Bluetooth instance;

    /**
     * Private constructor to prevent instantiation of the manager class.
     */
    private BluetoothManager() {}

    /**
     * Initializes and returns the singleton Bluetooth instance.
     * If the instance already exists, it updates the callback.
     *
     * @param context The application context.
     * @param callback A BluetoothCallback to handle Bluetooth events.
     * @return The singleton Bluetooth instance.
     */
    public static synchronized Bluetooth getInstance(Context context, Bluetooth.BluetoothCallback callback) {
        if (instance == null) {
            instance = new Bluetooth(context.getApplicationContext(), callback);
        } else {
            instance.setCallback(callback); // Update callback when switching fragments
        }
        return instance;
    }

    /**
     * Returns the current Bluetooth instance.
     * Must be called after {@link #getInstance(Context, Bluetooth.BluetoothCallback)} has been initialized.
     *
     * @return The current Bluetooth instance.
     * @throws IllegalStateException if the instance has not been initialized yet.
     */
    public static Bluetooth getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BluetoothManager not initialized. Call getInstance(context, callback) first.");
        }
        return instance;
    }

    /**
     * Cleans up and destroys the Bluetooth instance, releasing resources and unregistering receivers.
     * Requires BLUETOOTH_SCAN permission.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public static void destroyInstance() {
        if (instance != null) {
            instance.cleanup();
            instance = null;
        }
    }
}
