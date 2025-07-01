package htl.steyr.wechselgeldapp.Database.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Seller {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String shopName;
    public String email;
    public String passwordHash;
}

