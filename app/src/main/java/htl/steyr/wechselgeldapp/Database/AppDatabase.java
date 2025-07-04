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
import htl.steyr.wechselgeldapp.Database.Entity.Transaction;

// This class creates the local database for the app
// It holds the tables for customer, seller, device, balance and transaction
@Database(entities = {Customer.class, Seller.class, Device.class, Balance.class, Transaction.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    // Gives access to customer data (e.g., insert, delete, search)
    public abstract CustomerDao customerDao();

    // Gives access to seller data
    public abstract SellerDao sellerDao();

    // Gives access to device data (UUID, name, etc.)
    public abstract DeviceDao deviceDao();

    // Gives access to balance data (money balances between customer and seller)
    public abstract BalanceDao balanceDao();

    // Gives access to transactions (optional history, not mandatory in your app)
    public abstract TransactionDao transactionDao();
}
