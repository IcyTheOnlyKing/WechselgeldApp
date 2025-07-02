package htl.steyr.wechselgeldapp.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import htl.steyr.wechselgeldapp.Database.DataAccessObject.CustomerDao;
import htl.steyr.wechselgeldapp.Database.DataAccessObject.BalanceDao;
import htl.steyr.wechselgeldapp.Database.DataAccessObject.DeviceDao;
import htl.steyr.wechselgeldapp.Database.DataAccessObject.SellerDao;

import htl.steyr.wechselgeldapp.Database.DataAccessObject.TransactionDao;
import htl.steyr.wechselgeldapp.Database.Entity.Balance;
import htl.steyr.wechselgeldapp.Database.Entity.Customer;
import htl.steyr.wechselgeldapp.Database.Entity.Device;
import htl.steyr.wechselgeldapp.Database.Entity.Seller;


@Database(entities = {Customer.class, Seller.class, Device.class, Balance.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CustomerDao customerDao();

    public abstract SellerDao sellerDao();

    public abstract DeviceDao deviceDao();

    public abstract BalanceDao balanceDao();

    public abstract TransactionDao transactionDao();
}

