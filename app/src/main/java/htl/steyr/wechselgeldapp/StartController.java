package htl.steyr.wechselgeldapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

public class StartController extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Prüfen ob der User schon eingeloggt ist
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        String role = prefs.getString("user_role", "");

        if (isLoggedIn) {
            // User ist schon eingeloggt, direkt weiterleiten
            if (role.equals("seller")) {
                startActivity(new Intent(this, htl.steyr.wechselgeldapp.UI.SellerUIController.class));
            } else if (role.equals("customer")) {
                startActivity(new Intent(this, htl.steyr.wechselgeldapp.UI.CustomerUIController.class));
            }
            finish(); // Startseite beenden, damit kein Zurück möglich ist
            return;
        }

        // Standard: Startseite anzeigen
        setContentView(R.layout.start_view);

        Button customerBTN = findViewById(R.id.customerBTN);
        Button sellerBTN = findViewById(R.id.sellerBTN);

        customerBTN.setOnClickListener(view -> navigateTo("customer"));
        sellerBTN.setOnClickListener(view -> navigateTo("seller"));
    }

    // Weiterleitung zum Login/Register mit Rolle
    private void navigateTo(String role) {
        Intent intent = new Intent(StartController.this, AuthController.class);
        intent.putExtra("user_role", role);
        startActivity(intent);
    }
}
