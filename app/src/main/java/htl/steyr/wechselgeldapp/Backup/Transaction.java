package htl.steyr.wechselgeldapp.Backup;

public class Transaction {
    private String id;
    private double amount;
    private double changeGiven;
    private long timestamp;

    public Transaction() {
    }

    public Transaction(String id, double amount, double changeGiven, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.changeGiven = changeGiven;
        this.timestamp = timestamp;
    }

    // Getter und Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getChangeGiven() {
        return changeGiven;
    }

    public void setChangeGiven(double changeGiven) {
        this.changeGiven = changeGiven;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


}