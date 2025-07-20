package htl.steyr.wechselgeldapp.Database.Models;

public class Balance {
    private final String otherUuid;
    private final String displayName;
    private final double balance;
    private final long timestamp;

    public Balance(String otherUuid, String displayName, double balance, long timestamp) {
        this.otherUuid = otherUuid;
        this.displayName = displayName;
        this.balance = balance;
        this.timestamp = timestamp;
    }

    public String getOtherUuid() {
        return otherUuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBalance() {
        return balance;
    }

    public long getTimestamp() {
        return timestamp;
    }
}