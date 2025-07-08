package htl.steyr.wechselgeldapp.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * DatabaseHelper manages creation, versioning and CRUD operations for the local SQLite database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wechselgeld.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create all required tables when database is first created
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE Seller (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "shopName TEXT NOT NULL," +
                "email TEXT," +
                "passwordHash TEXT" +
                ");");

        db.execSQL("CREATE TABLE Customer (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "displayName TEXT NOT NULL," +
                "email TEXT," +
                "passwordHash TEXT" +
                ");");

        db.execSQL("CREATE TABLE Device (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "uuid TEXT NOT NULL UNIQUE," +
                "customerId INTEGER," +
                "sellerId INTEGER," +
                "deviceName TEXT," +
                "FOREIGN KEY (customerId) REFERENCES Customer(id)," +
                "FOREIGN KEY (sellerId) REFERENCES Seller(id)" +
                ");");

        db.execSQL("CREATE TABLE Balance (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "otherUuid TEXT NOT NULL," +
                "displayName TEXT," +
                "balance REAL," +
                "timestamp INTEGER" +
                ");");

        db.execSQL("CREATE TABLE Transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "amount REAL," +
                "timestamp INTEGER" +
                ");");
    }

    // Handles database upgrades (drops and recreates tables)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Transactions");
        db.execSQL("DROP TABLE IF EXISTS Balance");
        db.execSQL("DROP TABLE IF EXISTS Device");
        db.execSQL("DROP TABLE IF EXISTS Customer");
        db.execSQL("DROP TABLE IF EXISTS Seller");
        onCreate(db);
    }

    // ---------------- Seller CRUD ---------------- //

    /**
     * Inserts a new seller into the database.
     */
    public long insertSeller(String shopName, String email, String passwordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("shopName", shopName);
        values.put("email", email);
        values.put("passwordHash", passwordHash);
        return db.insert("Seller", null, values);
    }

    /**
     * Checks if a seller with the given shopName or email already exists.
     */
    public boolean sellerExists(String shopName, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM Seller WHERE shopName = ? OR email = ?", new String[]{shopName, email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * Returns the password hash for a given seller by shopName.
     */
    public String getSellerPasswordHash(String shopName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT passwordHash FROM Seller WHERE shopName = ?", new String[]{shopName});
        String hash = null;
        if (cursor.moveToFirst()) {
            hash = cursor.getString(0);
        }
        cursor.close();
        return hash;
    }

    // ---------------- Customer CRUD ---------------- //

    /**
     * Inserts a new customer into the database.
     */
    public long insertCustomer(String displayName, String email, String passwordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("displayName", displayName);
        values.put("email", email);
        values.put("passwordHash", passwordHash);
        return db.insert("Customer", null, values);
    }

    /**
     * Checks if a customer with the given displayName or email already exists.
     */
    public boolean customerExists(String displayName, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM Customer WHERE displayName = ? OR email = ?", new String[]{displayName, email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * Returns the password hash for a given customer by displayName.
     */
    public String getCustomerPasswordHash(String displayName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT passwordHash FROM Customer WHERE displayName = ?", new String[]{displayName});
        String hash = null;
        if (cursor.moveToFirst()) {
            hash = cursor.getString(0);
        }
        cursor.close();
        return hash;
    }

    // ---------------- Device CRUD ---------------- //

    /**
     * Inserts a device linked to either a customer or seller.
     */
    public long insertDevice(String uuid, Integer customerId, Integer sellerId, String deviceName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uuid", uuid);
        values.put("customerId", customerId);
        values.put("sellerId", sellerId);
        values.put("deviceName", deviceName);
        return db.insert("Device", null, values);
    }

    /**
     * Returns all devices in the database.
     */
    public Cursor getAllDevices() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Device", null);
    }

    /**
     * Deletes a device by UUID.
     */
    public int deleteDevice(String uuid) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("Device", "uuid = ?", new String[]{uuid});
    }

    // ---------------- Balance CRUD ---------------- //

    /**
     * Inserts or updates a balance record for a specific otherUuid.
     */
    public long insertOrUpdateBalance(String otherUuid, String displayName, double balance, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT id FROM Balance WHERE otherUuid = ?", new String[]{otherUuid});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            ContentValues values = new ContentValues();
            values.put("displayName", displayName);
            values.put("balance", balance);
            values.put("timestamp", timestamp);
            cursor.close();
            return db.update("Balance", values, "id = ?", new String[]{String.valueOf(id)});
        } else {
            ContentValues values = new ContentValues();
            values.put("otherUuid", otherUuid);
            values.put("displayName", displayName);
            values.put("balance", balance);
            values.put("timestamp", timestamp);
            cursor.close();
            return db.insert("Balance", null, values);
        }
    }

    /**
     * Returns all balance records.
     */
    public Cursor getAllBalances() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Balance", null);
    }

    /**
     * Returns a balance record for a specific otherUuid.
     */
    public Cursor getBalanceForUuid(String otherUuid) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Balance WHERE otherUuid = ?", new String[]{otherUuid});
    }

    /**
     * Deletes a balance record by UUID.
     */
    public int deleteBalance(String otherUuid) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("Balance", "otherUuid = ?", new String[]{otherUuid});
    }

    // ---------------- Transaction CRUD ---------------- //

    /**
     * Inserts a new transaction with amount and timestamp.
     */
    public long insertTransaction(double amount, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("timestamp", timestamp);
        return db.insert("Transactions", null, values);
    }

    /**
     * Returns all transaction records.
     */
    public Cursor getAllTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Transactions ORDER BY timestamp DESC", null);
    }

    /**
     * Deletes all transactions (useful for reset purposes).
     */
    public int deleteAllTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("Transactions", null, null);
    }

    // ---------------- End of CRUD ---------------- //
}
