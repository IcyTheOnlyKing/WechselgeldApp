package htl.steyr.wechselgeldapp.Database.Models;

public class Device {
    public int id;
    public String macAddress;
    public Integer customerId; // null if Seller
    public Integer sellerId;   // null if Customer
    public String deviceName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

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
