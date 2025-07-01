package htl.steyr.wechselgeldapp.Database.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Device {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String uuid;
    public int userId;
    public String userType; // "seller" oder "customer"
    public String deviceName;
}

