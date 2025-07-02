package htl.steyr.wechselgeldapp.Database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class AppDatabaseInstance {

    // Singleton instance of the database (only one instance for the whole app)
    private static AppDatabase instance;

    // Returns the database instance, creates it if it doesn't exist
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            // Builds the database with the name "wechselgeld-db"
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "wechselgeld-db"
                    )
                    .allowMainThreadQueries() // Allows DB operations on the main thread
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            // Disable Write-Ahead Logging so only one .db file is created (no WAL or SHM files)
                            db.disableWriteAheadLogging();
                        }
                    })
                    .build();
        }
        return instance;
    }
}
