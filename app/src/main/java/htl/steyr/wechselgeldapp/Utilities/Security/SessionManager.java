package htl.steyr.wechselgeldapp.Utilities.Security;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_USER_UUID = "user_uuid";

    public static void login(Context context, int userId, String userType, String displayName, String uuid) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TYPE, userType);
        editor.putString(KEY_DISPLAY_NAME, displayName);
        editor.putString(KEY_USER_UUID, uuid);
        editor.apply();
    }

    public static void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public static String getCurrentUserUuid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_UUID, null);
    }

    public static String getCurrentUserType(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_TYPE, null);
    }

    public static int getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public static String getCurrentDisplayName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_DISPLAY_NAME, "");
    }
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.contains(KEY_USER_ID) && prefs.contains(KEY_USER_TYPE);
    }
    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    public static void updateDisplayName(Context context, String newDisplayName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_DISPLAY_NAME, newDisplayName);
        editor.apply();
    }
    public static void updateUserType(Context context, String newUserType) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_TYPE, newUserType);
        editor.apply();
    }
    public static void updateUserUuid(Context context, String newUuid) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_UUID, newUuid);
        editor.apply();
    }
    public static void updateUserId(Context context, int newUserId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, newUserId);
        editor.apply();
    }
    public static void updateSession(Context context, int userId, String userType, String displayName, String uuid) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TYPE, userType);
        editor.putString(KEY_DISPLAY_NAME, displayName);
        editor.putString(KEY_USER_UUID, uuid);
        editor.apply();
    }
    public static boolean isCustomer(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String userType = prefs.getString(KEY_USER_TYPE, null);
        return "customer".equals(userType);
    }
    public static boolean isSeller(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String userType = prefs.getString(KEY_USER_TYPE, null);
        return "seller".equals(userType);
    }
    public static boolean isAdmin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String userType = prefs.getString(KEY_USER_TYPE, null);
        return "admin".equals(userType);
    }
    public static boolean isLoggedInAsCustomer(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.contains(KEY_USER_ID) && "customer".equals(prefs.getString(KEY_USER_TYPE, null));
    }
    public static boolean isLoggedInAsSeller(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.contains(KEY_USER_ID) && "seller".equals(prefs.getString(KEY_USER_TYPE, null));
    }
    public static boolean isLoggedInAsAdmin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.contains(KEY_USER_ID) && "admin".equals(prefs.getString(KEY_USER_TYPE, null));
    }

}