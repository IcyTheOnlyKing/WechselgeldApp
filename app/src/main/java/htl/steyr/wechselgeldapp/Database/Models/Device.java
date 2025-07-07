package htl.steyr.wechselgeldapp.Database.Models;

public class Device {
    public int id;
    public String macAddress;
    public Integer customerId; // null if Seller
    public Integer sellerId;   // null if Customer
    public String deviceName;

    public Device() {}

    public Device(int id, String macAddress, Integer customerId, Integer sellerId, String deviceName) {
        this.id = id;
        this.macAddress = macAddress;
        this.customerId = customerId;
        this.sellerId = sellerId;
        this.deviceName = deviceName;
    }

    public Device(String macAddress, Integer customerId, Integer sellerId, String deviceName) {
        this.macAddress = macAddress;
        this.customerId = customerId;
        this.sellerId = sellerId;
        this.deviceName = deviceName;
    }
}
