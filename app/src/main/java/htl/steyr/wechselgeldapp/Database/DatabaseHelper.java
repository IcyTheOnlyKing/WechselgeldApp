package htl.steyr.wechselgeldapp.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import htl.steyr.wechselgeldapp.Database.Models.Balance;
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

        db.execSQL("CREATE TABLE Seller (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "shopName TEXT NOT NULL," + "email TEXT," + "passwordHash TEXT" + ");");

        db.execSQL("CREATE TABLE Customer (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "displayName TEXT NOT NULL," + "email TEXT," + "passwordHash TEXT" + ");");

        db.execSQL("CREATE TABLE Device (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "uuid TEXT NOT NULL UNIQUE," + "customerId INTEGER," + "sellerId INTEGER," + "deviceName TEXT," + "FOREIGN KEY (customerId) REFERENCES Customer(id)," + "FOREIGN KEY (sellerId) REFERENCES Seller(id)" + ");");

        db.execSQL("CREATE TABLE Balance (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "otherUuid TEXT NOT NULL," + "displayName TEXT," + "balance REAL," + "timestamp INTEGER" + ");");

        db.execSQL("CREATE TABLE Transactions (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "amount REAL," + "timestamp INTEGER" + ");");

        db.execSQL("CREATE TABLE PersonalInformation (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "seller_id INTEGER NOT NULL," + "name TEXT," + "email TEXT," + "street TEXT," + "houseNumber TEXT," + "zipCode TEXT," + "city TEXT," + "FOREIGN KEY (seller_id) REFERENCES Seller(id) ON DELETE CASCADE" + ");");


        insertTestData();
    }


    // Handles database upgrades (drops and recreates tables)
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
     * Inserts a new seller into the database.
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
     * Returns the id of a seller by their shop name.
     */
    public int getSellerIdByName(String shopName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Seller WHERE shopName = ?", new String[]{shopName});

        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0); // Korrekt als Integer lesen
        }
        cursor.close();
        return id;
    }

    /**
     * Checks if a seller with the given shopName or email already exists.
     */
    public boolean sellerExists(String shopName, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Seller WHERE shopName = ? OR email = ?", new String[]{shopName, email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * Returns the password hash for a given seller by shopName.
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
     * Inserts a new customer into the database.
     */
    public void insertCustomer(String displayName, String email, String passwordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("displayName", displayName);
        values.put("email", email);
        values.put("passwordHash", passwordHash);
        db.insert("Customer", null, values);
    }


    /**
     * Checks if a customer with the given displayName or email already exists.
     */
    public boolean customerExists(String displayName, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Customer WHERE displayName = ? OR email = ?", new String[]{displayName, email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * Returns the password hash for a given customer by displayName.
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
     * Returns the id of a customer by their display name.
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
     * Returns the display name of a customer by id.
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

    /**
     * Returns the balance value for a specific customer id.
     */
    public Double getBalanceForCustomer(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT b.balance FROM Balance b JOIN Device d ON b.otherUuid = d.uuid WHERE d.customerId = ?", new String[]{String.valueOf(customerId)});
        Double balance = null;
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(0);
        }
        cursor.close();
        return balance;
    }

    // ---------------- PersonalInformation CRUD ---------------- //

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

    public Cursor getPersonalInfoBySellerId(int sellerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM PersonalInformation WHERE seller_id = ?", new String[]{String.valueOf(sellerId)});
    }

    public int updatePersonalInfo(int sellerId, String name, String email, String street, String houseNumber, String zipCode, String city) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);  // <-- Email hinzufügen
        values.put("street", street);
        values.put("houseNumber", houseNumber);
        values.put("zipCode", zipCode);
        values.put("city", city);
        return db.update("PersonalInformation", values, "seller_id = ?", new String[]{String.valueOf(sellerId)});
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

    /**
     * Inserts comprehensive and realistic test data into all database tables,
     * including admin accounts and profile data for sellers.
     * This method is called during database creation to populate initial data
     * for development and testing purposes.
     */
    public void insertTestData() {
        SQLiteDatabase db = this.getWritableDatabase();

        // --- Insert Sellers (including admin) ---
        db.execSQL("INSERT INTO Seller (shopName, email, passwordHash) VALUES " + "('admin', 'admin@seller.com', 'admin')," + "('Bäckerei Maier', 'maier@shop.com', 'b@eckMa2023')," + "('Kiosk Müller', 'mueller@kiosk.com', 'muellerSecure!')," + "('Trafik Schmid', 'schmid@trafik.com', 'schmid#456')," + "('Blumen Huber', 'huber@flowers.com', 'huberBloom22')," + "('Feinkost Hahn', 'hahn@finefood.com', 'fein#hahn2024');");

        // --- Insert Customers (including admin) ---
        db.execSQL("INSERT INTO Customer (displayName, email, passwordHash) VALUES " + "('admin', 'admin@customer.com', 'admin')," + "('Max Mustermann', 'max@web.de', 'maxSecure12')," + "('Erika Musterfrau', 'erika@web.de', 'erikaPass99')," + "('Lukas Lehner', 'lukas@web.at', 'lukas!strong')," + "('Anna Berger', 'anna@outlook.com', 'ann4Berger!')," + "('Thomas Meier', 'thomas@mail.com', 'th0mMe!')," + "('Julia König', 'julia@gmx.at', 'juKo2024!')," + "('Sebastian Kurz', 'sebastian@kurz.at', 'kurz1234')," + "('Nina Graf', 'nina@graf.net', 'ninaSafePass');");

        // --- Insert Devices linked to customers and sellers ---
        db.execSQL("INSERT INTO Device (uuid, customerId, sellerId, deviceName) VALUES " + "('uuid-admin-c', 1, NULL, 'Admin Kunden-Gerät')," + "('uuid-admin-s', NULL, 1, 'Admin Verkaufsgerät')," + "('uuid-max', 2, NULL, 'Max Handy')," + "('uuid-erika', 3, NULL, 'Erika Tablet')," + "('uuid-lukas', 4, NULL, 'Lukas Phone')," + "('uuid-anna', 5, NULL, 'Annas iPhone')," + "('uuid-seller1', NULL, 2, 'Maier Kasse 1')," + "('uuid-seller2', NULL, 3, 'Müller Kasse')," + "('uuid-seller3', NULL, 4, 'Trafik-Terminal')," + "('uuid-seller4', NULL, 5, 'Blumen Scanner')," + "('uuid-seller5', NULL, 6, 'Feinkost Terminal');");

        // --- Insert Balances for each device ---
        db.execSQL("INSERT INTO Balance (otherUuid, displayName, balance, timestamp) VALUES " + "('uuid-max', 'Max Mustermann', 25.50, 1720700000)," + "('uuid-erika', 'Erika Musterfrau', 15.75, 1720700050)," + "('uuid-lukas', 'Lukas Lehner', 48.20, 1720700100)," + "('uuid-anna', 'Anna Berger', 33.10, 1720700150)," + "('uuid-admin-c', 'Admin Kunde', 999.99, 1720700200)," + "('uuid-seller1', 'Bäckerei Maier', 120.00, 1720700250)," + "('uuid-seller2', 'Kiosk Müller', 180.40, 1720700300)," + "('uuid-seller3', 'Trafik Schmid', 95.10, 1720700350)," + "('uuid-seller4', 'Blumen Huber', 210.00, 1720700400)," + "('uuid-seller5', 'Feinkost Hahn', 310.50, 1720700450)," + "('uuid-admin-s', 'Admin Verkäufer', 999.99, 1720700500);");

        // --- Insert Sample Transactions with realistic variety ---
        db.execSQL("INSERT INTO Transactions (amount, timestamp) VALUES " + "(5.00, 1720701000)," + "(10.00, 1720701100)," + "(3.50, 1720701200)," + "(20.00, 1720701300)," + "(7.25, 1720701400)," + "(12.00, 1720701500)," + "(2.75, 1720701600)," + "(50.00, 1720701700)," + "(8.80, 1720701800)," + "(15.60, 1720701900)," + "(22.90, 1720702000)," + "(1.10, 1720702100)," + "(33.33, 1720702200)," + "(44.44, 1720702300)," + "(99.99, 1720702400);");

        // --- Insert Personal Information for all sellers (1:1 by seller ID) ---
        db.execSQL("INSERT INTO PersonalInformation (seller_id, name, email, street, houseNumber, zipCode, city) VALUES " + "(1, 'Admin Verkäufer', 'admin@seller.com', 'Adminstraße', '1A', '1010', 'Wien')," + "(2, 'Maier Bäcker', 'maier@shop.com', 'Brotgasse', '5', '4400', 'Steyr')," + "(3, 'Müller Kiosk', 'mueller@kiosk.com', 'Hauptstraße', '12B', '4020', 'Linz')," + "(4, 'Schmid Trafik', 'schmid@trafik.com', 'Tabakweg', '3', '5020', 'Salzburg')," + "(5, 'Huber Blumen', 'huber@flowers.com', 'Blumenweg', '7', '8010', 'Graz')," + "(6, 'Hahn Feinkost', 'hahn@finefood.com', 'Delikatessenallee', '9', '9020', 'Klagenfurt');");
    }
    /**
     * Gibt den aktuellsten Balance-Eintrag zurück.
     */
    public Balance getLatestBalance() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Balance ORDER BY timestamp DESC LIMIT 1", null);
        Balance balance = null;
        if (cursor.moveToFirst()) {
            balance = new Balance(
                    cursor.getString(cursor.getColumnIndexOrThrow("otherUuid")),
                    cursor.getString(cursor.getColumnIndexOrThrow("displayName")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("balance")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
            );
        }
        cursor.close();
        return balance;
    }

    /**
     * Fügt einen neuen Balance-Eintrag hinzu.
     */
    public void insertBalance(Balance balance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("otherUuid", balance.getOtherUuid());
        values.put("displayName", balance.getDisplayName());
        values.put("balance", balance.getBalance());
        values.put("timestamp", balance.getTimestamp());
        db.insert("Balance", null, values);
    }


}

