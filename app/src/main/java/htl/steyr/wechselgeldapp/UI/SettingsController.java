package htl.steyr.wechselgeldapp.UI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import htl.steyr.wechselgeldapp.AuthController;
import htl.steyr.wechselgeldapp.R;

public class SettingsController extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view);

        findViewById(R.id.logoutButton).setOnClickListener(v -> logout());


    }

    @SuppressLint("CommitPrefEdits")
    private void logout() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("is_logged_in", false).apply();  // .apply() speichert die Änderung

        Intent intent = new Intent(this, AuthController.class);
        startActivity(intent);

    }



}
