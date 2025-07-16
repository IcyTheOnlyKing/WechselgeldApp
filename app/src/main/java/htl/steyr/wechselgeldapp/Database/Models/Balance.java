package htl.steyr.wechselgeldapp.Database.Models;

public class Balance {
    public int id;
    public String otherUuid;
    public String displayName;
    public double balance;
    public long timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOtherUuidT() {
        return otherUuid;
    }

    public void setOtherUuid(String otherUuid) {
        this.otherUuid = otherUuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Balance() {}

    public Balance(int id, String otherUuid, String displayName, double balance, long timestamp) {
        this.id = id;
        this.otherUuid = otherUuid;
        this.displayName = displayName;
        this.balance = balance;
        this.timestamp = timestamp;
    }

    public Balance(String otherUuid, String displayName, double balance, long timestamp) {
        this.otherUuid = otherUuid;
        this.displayName = displayName;
        this.balance = balance;
        this.timestamp = timestamp;
    }
}
