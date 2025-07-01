package htl.steyr.wechselgeldapp.Database.DataAccessObject;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import htl.steyr.wechselgeldapp.Database.Entity.Customer;

@Dao
public interface CustomerDao {
    @Insert
    void insert(Customer customer);

    @Query("SELECT * FROM Customer WHERE id = :id")
    Customer getById(int id);

    @Query("SELECT * FROM Customer WHERE email = :email")
    Customer getByEmail(String email);

    @Query("SELECT * FROM Customer WHERE displayName = :displayName LIMIT 1")
    Customer findByDisplayName(String displayName);

    @Query("SELECT * FROM Customer WHERE email = :email LIMIT 1")
    Customer findByEmail(String email);
    @Query("DELETE FROM Customer")
    void deleteAll();
}

