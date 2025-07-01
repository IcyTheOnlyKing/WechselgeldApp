package htl.steyr.wechselgeldapp.Database.DataAccessObject;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


import htl.steyr.wechselgeldapp.Database.Entity.Balance;

@Dao
public interface BalanceDao {
    @Insert
    void insert(Balance balance);

    @Query("SELECT * FROM Balance WHERE id = :id")
    Balance getById(int id);

    @Query("SELECT * FROM Balance WHERE otherUuid = :uuid")
    Balance getByOtherUuid(String uuid);

    @Query("DELETE FROM Balance")
    void deleteAll();
}

