package htl.steyr.wechselgeldapp.Backup;

import java.util.Date;

public class Transaction {
    private String id;
    private double amount;
    private double changeGiven;
    private Date timestamp;
    private String description;

    public Transaction() {}

    public Transaction(String id, double amount, double changeGiven, Date timestamp, String description) {
        this.id = id;
        this.amount = amount;
        this.changeGiven = changeGiven;
        this.timestamp = timestamp;
        this.description = description;
    }

    // Getter und Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getChangeGiven() { return changeGiven; }
    public void setChangeGiven(double changeGiven) { this.changeGiven = changeGiven; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}