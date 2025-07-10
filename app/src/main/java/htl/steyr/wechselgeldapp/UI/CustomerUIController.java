package htl.steyr.wechselgeldapp.UI;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;
import java.util.Locale;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.R;

public class CustomerUIController extends Activity {

    private DatabaseHelper dbHelper;

    private MaterialToolbar topAppBar;
    private TextView textViewLastBalance;
    private TextView textViewTodayTransactionCount;
    private LinearLayout searchLayout;
    private LinearLayout settingsLayout;

    private String currentOtherUuid = "demo-uuid";  // → Muss aus deinem Login oder Intent kommen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_home_ui);

        // View-Verknüpfung
        topAppBar = findViewById(R.id.topAppBar);
        textViewLastBalance = findViewById(R.id.textViewLastBalance);
        textViewTodayTransactionCount = findViewById(R.id.textViewTodayTransactionCount);
        searchLayout = findViewById(R.id.searchBTN);
        settingsLayout = findViewById(R.id.settingsLayout);


        // DatabaseHelper initialisieren
        dbHelper = new DatabaseHelper(this);

        loadData();
    }

    private void loadData() {
        // Shopname anzeigen (nimmt ersten Seller aus DB)
        Cursor sellerCursor = dbHelper.getReadableDatabase().rawQuery("SELECT shopName FROM Seller LIMIT 1", null);
        String shopName = "Willkommen";
        if (sellerCursor.moveToFirst()) {
            shopName = sellerCursor.getString(0);
        }
        sellerCursor.close();
        topAppBar.setTitle(shopName);

        // Balance anzeigen
        Cursor balanceCursor = dbHelper.getBalanceForUuid(currentOtherUuid);
        String balanceText = "0,00 €";
        if (balanceCursor.moveToFirst()) {
            double balance = balanceCursor.getDouble(balanceCursor.getColumnIndexOrThrow("balance"));
            balanceText = String.format(Locale.getDefault(), "%.2f €", balance);
        }
        balanceCursor.close();
        textViewLastBalance.setText(balanceText);

        // Heutige Transaktionen zählen
        int transactionCount = getTransactionCountForToday();
        textViewTodayTransactionCount.setText(String.valueOf(transactionCount));

        searchLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchController.class);
            startActivity(intent);
        });

        settingsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsController.class);
            startActivity(intent);
        });

    }

    private int getTransactionCountForToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM Transactions WHERE timestamp >= ? AND timestamp < ?",
                new String[]{String.valueOf(startOfDay), String.valueOf(endOfDay)}
        );

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
