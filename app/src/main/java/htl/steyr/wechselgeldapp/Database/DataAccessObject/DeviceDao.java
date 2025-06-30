package htl.steyr.wechselgeldapp.Database.DataAccessObject;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import htl.steyr.wechselgeldapp.Database.Entity.Device;

@Dao
public interface DeviceDao {
    @Insert
    void insert(Device device);

    @Query("SELECT * FROM Device WHERE id = :id")
    Device getById(int id);

    @Query("SELECT * FROM Device WHERE uuid = :uuid")
    Device getByUuid(String uuid);

    @Query("DELETE FROM Device")
    void deleteAll();
}
