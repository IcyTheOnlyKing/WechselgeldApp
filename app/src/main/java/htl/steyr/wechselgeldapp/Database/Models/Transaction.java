package htl.steyr.wechselgeldapp.Database.Models;

public class Transaction {
    public int id;
    public double amount;
    public long timestamp;

    public Transaction() {}

    public Transaction(int id, double amount, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public Transaction(double amount, long timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
    }
}
