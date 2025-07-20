package htl.steyr.wechselgeldapp.Bluetooth;

public class TransactionMessage {
    public double amount;
    public String sellerName;
    public long timestamp;

    public TransactionMessage() {
    }

    public TransactionMessage(double amount, String sellerName, long timestamp) {
        this.amount = amount;
        this.sellerName = sellerName;
        this.timestamp = timestamp;
    }
}
