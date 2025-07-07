package htl.steyr.wechselgeldapp.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wechselgeld.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create Tables
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Transactions");
        db.execSQL("DROP TABLE IF EXISTS Balance");
        db.execSQL("DROP TABLE IF EXISTS Device");
        db.execSQL("DROP TABLE IF EXISTS Customer");
        db.execSQL("DROP TABLE IF EXISTS Seller");
        onCreate(db);
    }

    // ---------------- CRUD Methods ---------------- //


    public long insertSeller(String shopName, String email, String passwordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("shopName", shopName);
        values.put("email", email);
        values.put("passwordHash", passwordHash);
        return db.insert("Seller", null, values);
    }


    public long insertCustomer(String displayName, String email, String passwordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("displayName", displayName);
        values.put("email", email);
        values.put("passwordHash", passwordHash);
        return db.insert("Customer", null, values);
    }

    public long insertDevice(String uuid, Integer customerId, Integer sellerId, String deviceName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uuid", uuid);
        values.put("customerId", customerId);
        values.put("sellerId", sellerId);
        values.put("deviceName", deviceName);
        return db.insert("Device", null, values);
    }

    public long insertOrUpdateBalance(String otherUuid, String displayName, double balance, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT id FROM Balance WHERE otherUuid = ?", new String[]{otherUuid});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            ContentValues values = new ContentValues();
            values.put("displayName", displayName);
            values.put("balance", balance);
            values.put("timestamp", timestamp);
            int rows = db.update("Balance", values, "id = ?", new String[]{String.valueOf(id)});
            cursor.close();
            return rows;
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

    public long insertTransaction(double amount, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("timestamp", timestamp);
        return db.insert("Transactions", null, values);
    }

    public Cursor getAllBalances() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Balance", null);
    }

    public Cursor getBalanceForUuid(String otherUuid) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Balance WHERE otherUuid = ?", new String[]{otherUuid});
    }

    public Cursor getAllDevices() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Device", null);
    }

    // ---------------- Ende CRUD ---------------- //

}
