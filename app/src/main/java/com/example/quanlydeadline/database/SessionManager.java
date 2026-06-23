package com.example.quanlydeadline.database;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "quanlydeadline_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(int userId, String fullName) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_FULL_NAME, fullName)
                .apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
