package htl.steyr.wechselgeldapp.Backup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
        }
    }

    private File saveBackupToFile(String json) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMAN).format(new Date());
        String filename = BACKUP_FILE_PREFIX + timestamp + BACKUP_FILE_EXTENSION;

        File backupDir = new File(context.getFilesDir(), "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        File backupFile = new File(backupDir, filename);

        try (FileWriter writer = new FileWriter(backupFile)) {
            writer.write(json);
        }

        return backupFile;
    }

    private void sendBackupEmail(String userEmail, File backupFile) {
        try {
            Uri fileUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", backupFile);

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("application/json");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Wechselgeld App - Backup");
            emailIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hier ist dein automatisches Backup der Wechselgeld App.\n\n" +
                            "Erstellt am: " + new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN).format(new Date()));
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(emailIntent, "Backup senden");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);

        } catch (Exception e) {
            Log.e(TAG, "E-Mail-Versand fehlgeschlagen", e);
        }
    }

    public BackupData importBackup(String jsonString) throws Exception {
        return gson.fromJson(jsonString, BackupData.class);
    }

    public BackupData importBackupFromFile(File backupFile) throws Exception {
        try (FileReader reader = new FileReader(backupFile)) {
            return gson.fromJson(reader, BackupData.class);
        }
    }
}