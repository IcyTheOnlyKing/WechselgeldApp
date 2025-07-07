package htl.steyr.wechselgeldapp.Database.Models;

public class Balance {
    public int id;
    public String otherUuid;
    public String displayName;
    public double balance;
    public long timestamp;

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
