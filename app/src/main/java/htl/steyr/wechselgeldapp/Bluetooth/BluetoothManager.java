package htl.steyr.wechselgeldapp.Bluetooth;

import android.annotation.SuppressLint;
import android.content.Context;

public class BluetoothManager {
    private static Bluetooth instance;

    private BluetoothManager() {} // privater Konstruktor, um Instanziierung zu verhindern

    public static synchronized Bluetooth getInstance(Context context, Bluetooth.BluetoothCallback callback) {
        if (instance == null) {
            instance = new Bluetooth(context.getApplicationContext(), callback);
        } else {
            instance.setCallback(callback); // aktuelles Fragment setzt ggf. neuen Callback
        }
        return instance;
    }

    public static Bluetooth getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BluetoothManager wurde nicht initialisiert. Rufe zuerst getInstance(context, callback) auf.");
        }
        return instance;
    }

    public static void destroyInstance() {
        if (instance != null) {
            instance.cleanup();
            instance = null;
        }
    }
}
