package htl.steyr.wechselgeldapp.Utilities.Security;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {

    // Name of the SharedPreferences file where user session data is stored
    private static final String PREF_NAME = "user_prefs";

    // Private constructor to prevent instantiation of this utility class
    private SessionManager() {}

    /**
     * Saves the login state and user role in SharedPreferences.
     *
     * @param context The application context
     * @param role The role of the logged-in user ("seller" or "customer")
     */
    public static void saveLogin(Context context, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_role", role)
                .apply();
    }

    /**
     * Checks if the user is currently logged in.
     *
     * @param context The application context
     * @return True if the user is logged in, false otherwise
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("is_logged_in", false);
    }

    /**
     * Retrieves the stored user role from SharedPreferences.
     *
     * @param context The application context
     * @return The user role string ("seller", "customer", or empty if none saved)
     */
    public static String getUserRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString("user_role", "");
    }

    /**
     * Logs out the user by clearing all saved session data.
     *
     * @param context The application context
     */
    public static void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
