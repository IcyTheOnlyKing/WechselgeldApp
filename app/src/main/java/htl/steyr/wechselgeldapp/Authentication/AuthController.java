package htl.steyr.wechselgeldapp.Authentication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import htl.steyr.wechselgeldapp.R;

public class AuthController extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_view);

        // Rolle aus Intent auslesen
        String role = getIntent().getStringExtra("user_role");

        // TextView für das Label oben im Layout (muss in registration_view.xml existieren)
        TextView roleLabel = findViewById(R.id.roleLabel);

        TextInputEditText usernameInput = findViewById(R.id.usernameInput);

        if ("seller".equals(role)) {
            roleLabel.setText("Registrierung für Verkäufer");
            usernameInput.setHint("Geschäftsname");
        } else if ("customer".equals(role)) {
            roleLabel.setText("Registrierung für Kunden");
            usernameInput.setHint("Benutzername");
        } else {
            roleLabel.setText("Registrierung");
        }
    }
}
