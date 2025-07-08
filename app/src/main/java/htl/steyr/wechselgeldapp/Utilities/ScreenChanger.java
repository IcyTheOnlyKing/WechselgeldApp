package htl.steyr.wechselgeldapp.Utilities;

import android.app.Activity;
import android.content.Intent;

/**
 * A utility class for changing screens (activities) within the app.
 */
public class ScreenChanger {

    /**
     * Switches from the current screen (Activity) to a new one.
     *
     * @param main           The current activity (screen) from which we want to navigate.
     * @param targetActivity The activity class we want to open.
     */
    public static void changeScreen(Activity main, Class<? extends Activity> targetActivity) {
        // Create a new Intent to start the target activity.
        // An Intent is a message that Android uses to start new activities or communicate between components.
        Intent intent = new Intent(main, targetActivity);

        // Start the new activity.
        // This tells Android to launch the target screen.
        main.startActivity(intent);
    }
}
