package htl.steyr.wechselgeldapp.Database.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public double amount;
    public long timestamp;
    public String type; // "debit" oder "credit"

    public Transaction(double amount, long timestamp, String type) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.type = type;
    }
}