package htl.steyr.wechselgeldapp.Database.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Customer {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String displayName;
    public String email;
    public String passwordHash;
}
