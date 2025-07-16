package htl.steyr.wechselgeldapp.Bluetooth;

public class BluetoothManager {
    private static Bluetooth instance;

    private BluetoothManager() {}

    public static void setInstance(Bluetooth bluetooth) {
        instance = bluetooth;
    }

    public static Bluetooth getInstance() {
        return instance;
    }
}
