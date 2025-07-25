package htl.steyr.wechselgeldapp.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * DatabaseHelper manages the creation and maintenance of the local SQLite database.
 * It provides methods for CRUD operations on various tables like Seller, Customer,
 * Device, Balance, Transactions, and PersonalInformation.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wechselgeld.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of the database helper.
     *
     * @param context the application context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * Creates all required tables and inserts test data.
     *
     * @param db the writable database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all tables
        db.execSQL("CREATE TABLE Seller (id INTEGER PRIMARY KEY AUTOINCREMENT, shopName TEXT NOT NULL, email TEXT, passwordHash TEXT);");
        db.execSQL("CREATE TABLE Customer (id INTEGER PRIMARY KEY AUTOINCREMENT, displayName TEXT NOT NULL, email TEXT, passwordHash TEXT, balance REAL DEFAULT 0);");
        db.execSQL("CREATE TABLE Device (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL UNIQUE, customerId INTEGER, sellerId INTEGER, deviceName TEXT, FOREIGN KEY (customerId) REFERENCES Customer(id), FOREIGN KEY (sellerId) REFERENCES Seller(id));");
        db.execSQL("CREATE TABLE Balance (id INTEGER PRIMARY KEY AUTOINCREMENT, otherUuid TEXT NOT NULL, displayName TEXT, balance REAL, timestamp INTEGER);");
        db.execSQL("CREATE TABLE Transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL, timestamp INTEGER);");
        db.execSQL("CREATE TABLE PersonalInformation (id INTEGER PRIMARY KEY AUTOINCREMENT, seller_id INTEGER NOT NULL, name TEXT, email TEXT, street TEXT, houseNumber TEXT, zipCode TEXT, city TEXT, FOREIGN KEY (seller_id) REFERENCES Seller(id) ON DELETE CASCADE);");

        // Insert initial test data
        insertTestData(db);
    }

    /**
     * Called when the database version is upgraded.
     * Drops and recreates all tables.
     *
     * @param db         the database
     * @param oldVersion previous version number
     * @param newVersion new version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Transactions");
        db.execSQL("DROP TABLE IF EXISTS Balance");
        db.execSQL("DROP TABLE IF EXISTS Device");
        db.execSQL("DROP TABLE IF EXISTS Customer");
        db.execSQL("DROP TABLE IF EXISTS Seller");
        db.execSQL("DROP TABLE IF EXISTS PersonalInformation");
        onCreate(db);
    }

    // ---------------- Seller CRUD ---------------- //

    /**
     * Gets the name of the first shop in the database.
     *
     * @return the shop name or null if not found
     */
    public String getShopName() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT shopName FROM Seller LIMIT 1", null);

        String shopName = null;
        if (cursor.moveToFirst()) {
            shopName = cursor.getString(0);
        }
        cursor.close();
        return shopName;
    }

    /**
     * Inserts a new seller.
     */
    public void insertSeller(String shopName, String email, String passwordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("shopName", shopName);
        values.put("email", email);
        values.put("passwordHash", passwordHash);
        db.insert("Seller", null, values);
    }

    /**
     * Gets the ID of a seller using the shop name.
     */
    public int getSellerIdByName(String shopName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Seller WHERE shopName = ?", new String[]{shopName});
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    /**
     * Checks if a seller already exists by shop name or email.
     */
    public boolean sellerExists(String shopName, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Seller WHERE shopName = ? OR email = ?", new String[]{shopName, email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * Gets the password hash for a seller.
     */
    public String getSellerPasswordHash(String shopName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT passwordHash FROM Seller WHERE shopName = ?", new String[]{shopName});
        String hash = null;
        if (cursor.moveToFirst()) {
            hash = cursor.getString(0);
        }
        cursor.close();
        return hash;
    }

    // ---------------- Customer CRUD ---------------- //

    /**
     * Inserts a new customer.
     */
    public void insertCustomer(String displayName, String email, String passwordHash, double balance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("displayName", displayName);
        values.put("email", email);
        values.put("passwordHash", passwordHash);
        values.put("balance", balance);
        db.insert("Customer", null, values);
    }

    /**
     * Gets the current balance for a customer.
     */
    public Double getBalanceForCustomer(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT balance FROM Customer WHERE id = ?", new String[]{String.valueOf(customerId)});
        Double balance = null;
        if (cursor.moveToFirst()) {
            balance = cursor.isNull(0) ? null : cursor.getDouble(0);
        }
        cursor.close();
        return balance;
    }

    /**
     * Checks if a customer already exists by name or email.
     */
    public boolean customerExists(String displayName, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Customer WHERE displayName = ? OR email = ?", new String[]{displayName, email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * Gets the password hash for a customer.
     */
    public String getCustomerPasswordHash(String displayName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT passwordHash FROM Customer WHERE displayName = ?", new String[]{displayName});
        String hash = null;
        if (cursor.moveToFirst()) {
            hash = cursor.getString(0);
        }
        cursor.close();
        return hash;
    }

    /**
     * Gets the customer ID using their display name.
     */
    public String getCustomerIdByName(String displayName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Customer WHERE displayName = ?", new String[]{displayName});
        String id = null;
        if (cursor.moveToFirst()) {
            id = cursor.getString(0);
        }
        cursor.close();
        return id;
    }

    /**
     * Gets the display name of a customer using their ID.
     */
    public String getCustomerNameById(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT displayName FROM Customer WHERE id = ?", new String[]{String.valueOf(customerId)});
        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    // ---------------- PersonalInformation CRUD ---------------- //

    /**
     * Inserts personal information for a seller.
     */
    public long insertPersonalInfo(int sellerId, String name, String email, String street, String houseNumber, String zipCode, String city) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("seller_id", sellerId);
        values.put("name", name);
        values.put("email", email);
        values.put("street", street);
        values.put("houseNumber", houseNumber);
        values.put("zipCode", zipCode);
        values.put("city", city);
        return db.insert("PersonalInformation", null, values);
    }

    /**
     * Gets the personal information of a seller.
     */
    public Cursor getPersonalInfoBySellerId(int sellerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM PersonalInformation WHERE seller_id = ?", new String[]{String.valueOf(sellerId)});
    }

    /**
     * Updates the personal information of a seller.
     */
    public int updatePersonalInfo(int sellerId, String name, String email, String street, String houseNumber, String zipCode, String city) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("street", street);
        values.put("houseNumber", houseNumber);
        values.put("zipCode", zipCode);
        values.put("city", city);
        return db.update("PersonalInformation", values, "seller_id = ?", new String[]{String.valueOf(sellerId)});
    }

    // ---------------- Device CRUD ---------------- //

    /**
     * Inserts a new device entry.
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
     * Saves a connected device if not already stored.
     */
    public void saveConnectedDevice(String macAddress, String deviceName, Integer customerId, Integer sellerId) {
        if (macAddress == null) return;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Device WHERE uuid = ?", new String[]{macAddress});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (!exists) {
            ContentValues values = new ContentValues();
            values.put("uuid", macAddress);
            values.put("deviceName", deviceName);
            values.put("customerId", customerId);
            values.put("sellerId", sellerId);
            db.insert("Device", null, values);
        }
    }

    /**
     * Gets all device entries.
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
     * Inserts or updates a balance entry.
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
     * Gets all balance entries.
     */
    public Cursor getAllBalances() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Balance", null);
    }

    /**
     * Gets balance entry for a specific UUID.
     */
    public Cursor getBalanceForUuid(String otherUuid) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Balance WHERE otherUuid = ?", new String[]{otherUuid});
    }

    /**
     * Deletes a balance entry by UUID.
     */
    public int deleteBalance(String otherUuid) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("Balance", "otherUuid = ?", new String[]{otherUuid});
    }

    // ---------------- Transaction CRUD ---------------- //

    /**
     * Inserts a new transaction entry.
     */
    public long insertTransaction(double amount, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("timestamp", timestamp);
        return db.insert("Transactions", null, values);
    }

    /**
     * Gets all transaction records sorted by newest.
     */
    public Cursor getAllTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Transactions ORDER BY timestamp DESC", null);
    }

    /**
     * Deletes all transactions from the table.
     */
    public int deleteAllTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("Transactions", null, null);
    }

    /**
     * Inserts demo data into all tables for testing purposes.
     *
     * @param db the writable database
     */
    public void insertTestData(SQLiteDatabase db) {

        // --- Seller ---
        db.execSQL("INSERT INTO Seller (shopName, email, passwordHash) VALUES " + "('admin', 'admin@seller.com', 'admin')," + "('Bäckerei Maier', 'maier@shop.com', 'b@eckMa2023')," + "('Kiosk Müller', 'mueller@kiosk.com', 'muellerSecure!')," + "('Trafik Schmid', 'schmid@trafik.com', 'schmid#456')," + "('Blumen Huber', 'huber@flowers.com', 'huberBloom22')," + "('Feinkost Hahn', 'hahn@finefood.com', 'fein#hahn2024');");

        // --- Customer ---
        db.execSQL("INSERT INTO Customer (displayName, email, passwordHash, balance) VALUES " + "('admin', 'admin@customer.com', 'admin', 0.00)," + "('Max Mustermann', 'max@web.de', 'maxSecure12', 12.50)," + "('Erika Musterfrau', 'erika@web.de', 'erikaPass99', 7.25)," + "('Lukas Lehner', 'lukas@web.at', 'lukas!strong', 3.00)," + "('Anna Berger', 'anna@outlook.com', 'ann4Berger!', 0.00)," + "('Thomas Meier', 'thomas@mail.com', 'th0mMe!', 5.00)," + "('Julia König', 'julia@gmx.at', 'juKo2024!', 8.20)," + "('Sebastian Kurz', 'sebastian@kurz.at', 'kurz1234', 15.00)," + "('Nina Graf', 'nina@graf.net', 'ninaSafePass', 2.75);");

        // --- Transactions ---
        db.execSQL("INSERT INTO Transactions (amount, timestamp) VALUES " + "(5.00, 1720701000)," + "(10.00, 1720701100)," + "(3.50, 1720701200)," + "(20.00, 1720701300)," + "(7.25, 1720701400)," + "(12.00, 1720701500)," + "(2.75, 1720701600)," + "(50.00, 1720701700)," + "(8.80, 1720701800)," + "(15.60, 1720701900)," + "(22.90, 1720702000)," + "(1.10, 1720702100)," + "(33.33, 1720702200)," + "(44.44, 1720702300)," + "(99.99, 1720702400);");

        // --- Personal Information ---
        db.execSQL("INSERT INTO PersonalInformation (seller_id, name, email, street, houseNumber, zipCode, city) VALUES " + "(1, 'Admin Verkäufer', 'admin@seller.com', 'Adminstraße', '1A', '1010', 'Wien')," + "(2, 'Maier Bäcker', 'maier@shop.com', 'Brotgasse', '5', '4400', 'Steyr')," + "(3, 'Müller Kiosk', 'mueller@kiosk.com', 'Hauptstraße', '12B', '4020', 'Linz')," + "(4, 'Schmid Trafik', 'schmid@trafik.com', 'Tabakweg', '3', '5020', 'Salzburg')," + "(5, 'Huber Blumen', 'huber@flowers.com', 'Blumenweg', '7', '8010', 'Graz')," + "(6, 'Hahn Feinkost', 'hahn@finefood.com', 'Delikatessenallee', '9', '9020', 'Klagenfurt');");

    }

    /**
     * Updates the basic seller profile (name and email).
     */
    public void updateSellerProfile(int sellerId, String shopName, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("shopName", shopName);
        values.put("email", email);
        db.update("Seller", values, "id = ?", new String[]{String.valueOf(sellerId)});
    }

    /**
     * Updates the basic customer profile (name and email).
     */
    public void updateCustomerProfile(int customerId, String displayName, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("displayName", displayName);
        values.put("email", email);
        db.update("Customer", values, "id = ?", new String[]{String.valueOf(customerId)});
    }

    /**
     * Combines seller and personal data into one result.
     */
    public Cursor getSellerProfile(int sellerId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT s.shopName, s.email, p.name, p.street, p.houseNumber, p.zipCode, p.city " + "FROM Seller s LEFT JOIN PersonalInformation p ON s.id = p.seller_id WHERE s.id = ?", new String[]{String.valueOf(sellerId)});
    }


    /**
     * Retrieves the name and email of a customer from the database by their ID.
     *
     * @param customerId The ID of the customer whose profile is being requested.
     * @return A {@link Cursor} pointing to the result set with columns: displayName and email.
     */
    public Cursor getCustomerProfile(int customerId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT displayName, email FROM Customer WHERE id = ?",
                new String[]{String.valueOf(customerId)}
        );
    }


}