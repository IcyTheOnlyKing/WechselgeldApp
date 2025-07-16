package htl.steyr.wechselgeldapp.Backup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupManager {
    private static final String TAG = "BackupManager";
    private static final String BACKUP_FILE_PREFIX = "wechselgeld_backup_";
    private static final String BACKUP_FILE_EXTENSION = ".json";

    private final Context context;
    private final Gson gson;

    public BackupManager(Context context) {
        this.context = context;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    public void createAndSendBackup(String userEmail, List<Transaction> transactions, UserData userData) {
        try {
            BackupData backupData = new BackupData(transactions, userData, new Date());
            String json = gson.toJson(backupData);

            File backupFile = saveBackupToFile(json);
            sendBackupEmail(userEmail, backupFile);

        } catch (Exception e) {
            Log.e(TAG, "Backup-Erstellung fehlgeschlagen", e);
            throw new RuntimeException("Backup konnte nicht erstellt werden", e);
        }
    }

    private File saveBackupToFile(String json) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMAN).format(new Date());
        String filename = BACKUP_FILE_PREFIX + timestamp + BACKUP_FILE_EXTENSION;

        // Backup-Ordner erstellen
        File backupDir = new File(context.getFilesDir(), "backups");
        if (!backupDir.exists()) {
            boolean created = backupDir.mkdirs();
            if (!created) {
                throw new IOException("Backup-Ordner konnte nicht erstellt werden");
            }
        }

        File backupFile = new File(backupDir, filename);

        // JSON in Datei schreiben
        try (FileWriter writer = new FileWriter(backupFile)) {
            writer.write(json);
        }

        Log.d(TAG, "Backup gespeichert: " + backupFile.getAbsolutePath());
        return backupFile;
    }

    private void sendBackupEmail(String userEmail, File backupFile) {
        try {
            // FileProvider URI generieren
            Uri fileUri = FileProvider.getUriForFile(context,
                    "htl.steyr.wechselgeldapp.fileprovider", backupFile);

            // E-Mail Intent erstellen
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("application/json");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Wechselgeld App - Backup");
            emailIntent.putExtra(Intent.EXTRA_TEXT, createEmailBody());
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Chooser mit Intent starten
            Intent chooser = Intent.createChooser(emailIntent, "Backup senden");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);

            Log.d(TAG, "E-Mail Intent gestartet");

        } catch (Exception e) {
            Log.e(TAG, "E-Mail-Versand fehlgeschlagen", e);
            throw new RuntimeException("E-Mail konnte nicht gesendet werden", e);
        }
    }

    private String createEmailBody() {
        return "Hier ist dein automatisches Backup der Wechselgeld App.\n\n" +
                "Erstellt am: " + new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN).format(new Date()) + "\n" +
                "App-Version: 1.0\n" +
                "Android-Version: " + Build.VERSION.RELEASE + "\n\n" +
                "Importiere diese Datei über die App-Einstellungen, um deine Daten wiederherzustellen.";
    }

    public BackupData importBackup(String jsonString) throws Exception {
        try {
            BackupData backupData = gson.fromJson(jsonString, BackupData.class);
            if (backupData == null) {
                throw new IllegalArgumentException("Backup-Daten sind ungültig");
            }
            return backupData;
        } catch (Exception e) {
            Log.e(TAG, "Backup-Import fehlgeschlagen", e);
            throw new Exception("Backup-Datei konnte nicht gelesen werden", e);
        }
    }

    public BackupData importBackupFromFile(File backupFile) throws Exception {
        if (!backupFile.exists()) {
            throw new FileNotFoundException("Backup-Datei nicht gefunden");
        }

        try (FileReader reader = new FileReader(backupFile)) {
            BackupData backupData = gson.fromJson(reader, BackupData.class);
            if (backupData == null) {
                throw new IllegalArgumentException("Backup-Daten sind ungültig");
            }
            return backupData;
        } catch (Exception e) {
            Log.e(TAG, "Backup-Import aus Datei fehlgeschlagen", e);
            throw new Exception("Backup-Datei konnte nicht gelesen werden", e);
        }
    }
    public void createAndSendBackup(String userEmail,
                                    List<Transaction> transactions,
                                    UserData userData,
                                    String password) {  // Passwort als Parameter

        BackupData backupData = new BackupData(transactions, userData, new Date());
        String json = gson.toJson(backupData);

        try {
            // Daten verschlüsseln
            String encryptedJson = EncryptionUtil.encrypt(json, password);
            File backupFile = saveBackupToFile(encryptedJson);  // Speichern der verschlüsselten Daten
            sendBackupEmail(userEmail, backupFile);

        } catch (Exception e) {
            Log.e(TAG, "Verschlüsselung fehlgeschlagen", e);
            throw new RuntimeException("Backup konnte nicht verschlüsselt werden", e);
        }
    }

    public BackupData importBackup(String jsonString, String password) throws Exception {
        try {
            // Daten entschlüsseln
            String decryptedJson = EncryptionUtil.decrypt(jsonString, password);
            return gson.fromJson(decryptedJson, BackupData.class);

        } catch (Exception e) {
            Log.e(TAG, "Entschlüsselung fehlgeschlagen", e);
            throw new Exception("Falsches Passwort oder beschädigte Datei", e);
        }
    }

    // Hilfsmethode zum Löschen alter Backups
    public void cleanupOldBackups(int maxBackups) {
        File backupDir = new File(context.getFilesDir(), "backups");
        if (!backupDir.exists()) return;

        File[] backupFiles = backupDir.listFiles((dir, name) ->
                name.startsWith(BACKUP_FILE_PREFIX) && name.endsWith(BACKUP_FILE_EXTENSION));

        if (backupFiles != null && backupFiles.length > maxBackups) {
            // Nach Datum sortieren (neueste zuerst)
            java.util.Arrays.sort(backupFiles, (f1, f2) ->
                    Long.compare(f2.lastModified(), f1.lastModified()));

            // Alte Backups löschen
            for (int i = maxBackups; i < backupFiles.length; i++) {
                boolean deleted = backupFiles[i].delete();
                Log.d(TAG, "Altes Backup gelöscht: " + backupFiles[i].getName() + " - " + deleted);
            }
        }
    }
}