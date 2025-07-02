package htl.steyr.wechselgeldapp.Database.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Balance {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String otherUuid;
    public String displayName;
    public double balance;
    public long timestamp;

}