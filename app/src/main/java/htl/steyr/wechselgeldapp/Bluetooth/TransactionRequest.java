package htl.steyr.wechselgeldapp.Bluetooth;

public class TransactionRequest {
    private String senderUuid;
    private String receiverUuid;
    private double amount;
    private long timestamp;

    public TransactionRequest(String senderUuid, String receiverUuid, double amount) {
        this.senderUuid = senderUuid;
        this.receiverUuid = receiverUuid;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSenderUuid() {
        return senderUuid;
    }

    public void setSenderUuid(String senderUuid) {
        this.senderUuid = senderUuid;
    }

    public String getReceiverUuid() {
        return receiverUuid;
    }

    public void setReceiverUuid(String receiverUuid) {
        this.receiverUuid = receiverUuid;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}