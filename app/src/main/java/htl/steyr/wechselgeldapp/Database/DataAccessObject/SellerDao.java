package htl.steyr.wechselgeldapp.Database.DataAccessObject;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import htl.steyr.wechselgeldapp.Database.Entity.Seller;

@Dao
public interface SellerDao {
    @Insert void insert(Seller seller);
    @Query("SELECT * FROM Seller WHERE shopName = :shopName LIMIT 1")
    Seller findByShopName(String shopName);

    @Query("SELECT * FROM Seller WHERE email = :email LIMIT 1")
    Seller findByEmail(String email);
    @Query("SELECT * FROM Seller WHERE id = :id")
    Seller getById(int id);


    @Query("DELETE FROM Seller")
    void deleteAll();
}