package htl.steyr.wechselgeldapp.Bluetooth;

import android.content.Context;
import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth.BluetoothCallback;

public class BluetoothManager {
    private static Bluetooth instance;
    private static BluetoothCallback globalCallback;

    private BluetoothManager() {}

    public static void setInstance(Bluetooth bluetooth) {
        instance = bluetooth;
        if (globalCallback != null) {
            instance.setCallback(globalCallback);
        }
    }

    public static Bluetooth getInstance() {
        return instance;
    }

    public static void setCallback(BluetoothCallback callback) {
        globalCallback = callback;
        if (instance != null) {
            instance.setCallback(globalCallback);
        }
    }
}