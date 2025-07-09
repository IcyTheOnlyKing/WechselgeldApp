package htl.steyr.wechselgeldapp.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import htl.steyr.wechselgeldapp.Backup.BackupData;
import htl.steyr.wechselgeldapp.Backup.BackupManager;
import htl.steyr.wechselgeldapp.Backup.Transaction;
import htl.steyr.wechselgeldapp.Backup.UserData;
import htl.steyr.wechselgeldapp.R;

public class SettingsController extends AppCompatActivity {
    private BackupManager backupManager;
    private String userEmail = "user@example.com"; // Aus Login laden

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        backupManager = new BackupManager(this);
        setupFilePickerLauncher();
        setupButtons();
    }

    private void setupFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        importBackupFromUri(uri);
                    }
                }
        );
    }

    private void setupButtons() {
        Button btnCreateBackup = findViewById(R.id.btn_create_backup);
        Button btnImportBackup = findViewById(R.id.btn_import_backup);

        btnCreateBackup.setOnClickListener(v -> createManualBackup());
        btnImportBackup.setOnClickListener(v -> selectBackupFile());
    }

    private void createManualBackup() {
        try {
            List<Transaction> transactions = getTransactionHistory();
            UserData userData = getCurrentUserData();

            backupManager.createAndSendBackup(userEmail, transactions, userData);
            Toast.makeText(this, "Backup wird erstellt und per E-Mail gesendet", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("Settings", "Backup-Erstellung fehlgeschlagen", e);
            Toast.makeText(this, "Backup-Erstellung fehlgeschlagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectBackupFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Backup-Datei auswählen"));
    }

    private void importBackupFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            String jsonString = stringBuilder.toString();
            BackupData backupData = backupManager.importBackup(jsonString);

            restoreDataFromBackup(backupData);
            Toast.makeText(this, "Backup erfolgreich importiert", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("Settings", "Backup-Import fehlgeschlagen", e);
            Toast.makeText(this, "Backup-Import fehlgeschlagen", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Transaction> getTransactionHistory() {
        List<Transaction> transactions = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("TransactionData", MODE_PRIVATE);

        int transactionCount = prefs.getInt("transaction_count", 0);

        for (int i = 0; i < transactionCount; i++) {
            String amount = prefs.getString("transaction_amount_" + i, "0.0");
            String changeGiven = prefs.getString("transaction_change_" + i, "0.0");
            long timestamp = prefs.getLong("transaction_timestamp_" + i, System.currentTimeMillis());

            // Transaction ohne Parameter erstellen und Werte setzen
            Transaction transaction = new Transaction();
            transaction.setAmount(Double.parseDouble(amount));
            transaction.setChangeGiven(Double.parseDouble(changeGiven));
            transaction.setTimestamp(timestamp);

            transactions.add(transaction);
        }

        return transactions;
    }

    private double getTotalAmount() {
        SharedPreferences prefs = getSharedPreferences("TransactionData", MODE_PRIVATE);
        return Double.parseDouble(prefs.getString("total_amount", "0.0"));
    }

    private int getTransactionCount() {
        SharedPreferences prefs = getSharedPreferences("TransactionData", MODE_PRIVATE);
        return prefs.getInt("transaction_count", 0);
    }

    private void saveTransactionsToDatabase(List<Transaction> transactions) {
        SharedPreferences prefs = getSharedPreferences("TransactionData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Alte Daten löschen
        editor.clear();

        // Neue Transaktionen speichern
        editor.putInt("transaction_count", transactions.size());

        double totalAmount = 0.0;
        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            editor.putString("transaction_amount_" + i, String.valueOf(transaction.getAmount()));
            editor.putString("transaction_change_" + i, String.valueOf(transaction.getChangeGiven()));
            editor.putLong("transaction_timestamp_" + i, transaction.getTimestamp());

            totalAmount += transaction.getAmount();
        }

        editor.putString("total_amount", String.valueOf(totalAmount));
        editor.apply();
    }

    private void saveUserData(UserData userData) {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user_email", userData.getEmail());
        editor.putString("user_name", userData.getUsername()); // oder userData.getName() falls verfügbar
        editor.putString("total_amount", String.valueOf(userData.getTotalAmmunt())); // oder userData.getTotalAmount()
        editor.putInt("transaction_count", userData.getTransactionCount());

        editor.apply();
    }
    private UserData getCurrentUserData() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String email = prefs.getString("user_email", userEmail);
        String name = prefs.getString("user_name", "Benutzername");
        double totalAmount = getTotalAmount();
        int transactionCount = getTransactionCount();

        return new UserData(email, name, totalAmount, transactionCount);
    }

    private void restoreDataFromBackup(BackupData backupData) {
        List<Transaction> transactions = backupData.getTransactions();
        UserData userData = backupData.getUserData();

        // Daten in deine App-Struktur zurückschreiben
        saveTransactionsToDatabase(transactions);
        saveUserData(userData);
    }
}