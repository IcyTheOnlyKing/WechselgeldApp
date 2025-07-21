package htl.steyr.wechselgeldapp.Database.Models;

public class Device {
    public int id;
    public String uuid;
    public Integer customerId; // null if Seller
    public Integer sellerId;   // null if Customer
    public String deviceName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getuuid() {
        return uuid;
    }

    public void setuuid(String uuid) {
        this.uuid = uuid;
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

    public Device(int id, String uuid, Integer customerId, Integer sellerId, String deviceName) {
        this.id = id;
        this.uuid = uuid;
        this.customerId = customerId;
        this.sellerId = sellerId;
        this.deviceName = deviceName;
    }

    public Device(String uuid, Integer customerId, Integer sellerId, String deviceName) {
        this.uuid = uuid;
        this.customerId = customerId;
        this.sellerId = sellerId;
        this.deviceName = deviceName;
    }
}
