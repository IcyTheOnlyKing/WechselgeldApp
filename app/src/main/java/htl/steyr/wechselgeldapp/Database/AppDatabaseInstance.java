package htl.steyr.wechselgeldapp.Database;

import android.content.Context;
import androidx.room.Room;

public class AppDatabaseInstance {

    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "wechselgeld-db"
            ).allowMainThreadQueries().build();
        }
        return instance;
    }
}

