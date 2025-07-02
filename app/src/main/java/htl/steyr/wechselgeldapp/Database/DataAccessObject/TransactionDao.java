package htl.steyr.wechselgeldapp.Database.DataAccessObject;

import androidx.room.Dao;
import androidx.room.Query;
import htl.steyr.wechselgeldapp.Database.Entity.Transaction;

@Dao
public interface TransactionDao {
    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    int getTransactionCountForDay(long startOfDay, long endOfDay);
}