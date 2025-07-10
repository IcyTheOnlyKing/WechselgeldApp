package htl.steyr.wechselgeldapp.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private static final int REQUEST_STORAGE_PERMISSION = 1001;

    private BackupManager backupManager;
    private final String userEmail = "user@example.com"; // Aus Login laden

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

    private void selectBackupFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Backup-Datei auswählen"));
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

    private UserData getCurrentUserData() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String email = prefs.getString("user_email", userEmail);
        String name = prefs.getString("user_name", "Benutzername");
        double totalAmount = getTotalAmount();
        int transactionCount = getTransactionCount();

        return new UserData(email, name, totalAmount, transactionCount);  // Korrigiert: Parameter-Reihenfolge
    }

    // Korrigierte saveUserData Methode
    private void saveUserData(UserData userData) {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user_email", userData.getEmail());
        editor.putString("user_name", userData.getUsername());
        editor.putString("total_amount", String.valueOf(userData.getTotalAmount()));  // Korrigiert: getTotalAmount()
        editor.putInt("transaction_count", userData.getTransactionCount());

        editor.apply();
    }

    @SuppressLint("ObsoleteSdkInt")
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted - you can now perform file operations
                Toast.makeText(this, "Berechtigung erteilt", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Berechtigung verweigert", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void restoreDataFromBackup(BackupData backupData) {
        List<Transaction> transactions = backupData.getTransactions();
        UserData userData = backupData.getUserData();

        // Daten in deine App-Struktur zurückschreiben
        saveTransactionsToDatabase(transactions);
        saveUserData(userData);
    }

    private void createManualBackup() {
        // Passwort-Eingabe-Dialog anzeigen
        showPasswordDialog(true);
    }

    private void importBackupFromUri(Uri uri) {
        try {
            // Dateiinhalt lesen
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String encryptedJson = stringBuilder.toString();

            // Passwort für Entschlüsselung abfragen
            showPasswordDialog(false, encryptedJson);

        } catch (Exception e) {
            Log.e("Settings", "Dateilesefehler", e);
            Toast.makeText(this, "Backup-Datei konnte nicht gelesen werden", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPasswordDialog(boolean isForBackup, String... importData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Passwort erforderlich");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString();
            if (isForBackup) {
                createBackupWithPassword(password);
            } else {
                decryptAndImport(importData[0], password);
            }
        });

        builder.setNegativeButton("Abbrechen", null);
        builder.show();
    }

    private void createBackupWithPassword(String password) {
        try {
            List<Transaction> transactions = getTransactionHistory();
            UserData userData = getCurrentUserData();
            backupManager.createAndSendBackup(userEmail, transactions, userData, password);
            Toast.makeText(this, "Verschlüsseltes Backup wird erstellt", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Settings", "Backup-Erstellung fehlgeschlagen", e);
            Toast.makeText(this, "Backup fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void decryptAndImport(String encryptedJson, String password) {
        try {
            BackupData backupData = backupManager.importBackup(encryptedJson, password);
            restoreDataFromBackup(backupData);
            Toast.makeText(this, "Backup erfolgreich importiert", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Settings", "Backup-Import fehlgeschlagen", e);
            Toast.makeText(this, "Import fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}