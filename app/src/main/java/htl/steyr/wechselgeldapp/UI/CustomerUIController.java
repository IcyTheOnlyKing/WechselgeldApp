package htl.steyr.wechselgeldapp.UI;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;
import java.util.Locale;

import htl.steyr.wechselgeldapp.Database.AppDatabase;
import htl.steyr.wechselgeldapp.Database.DataAccessObject.BalanceDao;
import htl.steyr.wechselgeldapp.Database.DataAccessObject.SellerDao;
import htl.steyr.wechselgeldapp.Database.DataAccessObject.TransactionDao;
import htl.steyr.wechselgeldapp.Database.Entity.Balance;
import htl.steyr.wechselgeldapp.Database.Entity.Seller;
import htl.steyr.wechselgeldapp.R;

public class CustomerUIController extends AppCompatActivity {

    private AppDatabase db;
    private SellerDao sellerDao;
    private BalanceDao balanceDao;

    private MaterialToolbar topAppBar;
    private TextView textViewLastBalance;
    private TextView textViewTodayTransactionCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_ui);

        // View-Verknüpfung
        topAppBar = findViewById(R.id.topAppBar);
        textViewLastBalance = findViewById(R.id.textViewLastBalance);
        textViewTodayTransactionCount = findViewById(R.id.textViewTodayTransactionCount);

        // Room-DB starten
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "wechselgeld-db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        sellerDao = db.sellerDao();
        balanceDao = db.balanceDao();

        loadData();
    }

    private void loadData() {
        // Shopname setzen
        Seller seller = sellerDao.getById(1);
        String shopName;
        if (seller != null) shopName = seller.shopName;
        else shopName = "Willkommen";
        topAppBar.setTitle(shopName);

        // Balance anzeigen
        Balance balance = balanceDao.getById(1);
        String balanceText;
        if (balance != null)
            balanceText = String.format(Locale.getDefault(), "%.2f €", balance.balance);
        else balanceText = "0,00 €";
        textViewLastBalance.setText(balanceText);

        // Heutige Transaktionen (später aus DB)
        int transactionCount = getTransactionCountForToday();
        textViewTodayTransactionCount.setText(String.valueOf(transactionCount));
    }

    private int getTransactionCountForToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        // Ende des Tages (23:59:59)
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();

        // Datenbankabfrage
        TransactionDao transactionDao = db.transactionDao();
        return transactionDao.getTransactionCountForDay(startOfDay, endOfDay);
    }
}