package htl.steyr.wechselgeldapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.UI.CustomerUIController;
import htl.steyr.wechselgeldapp.UI.SellerUIController;
import htl.steyr.wechselgeldapp.Utilities.Security.SecureData;
import htl.steyr.wechselgeldapp.Utilities.Security.SessionManager;

import org.mindrot.jbcrypt.BCrypt;

public class AuthController extends Activity {

    private int currentLayoutResId;
    private String role; // "seller" oder "customer"
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(getApplicationContext());

        role = getIntent().getStringExtra("user_role");
        showRegistrationView();
    }

    private void showRegistrationView() {
        currentLayoutResId = R.layout.registration_view;
        setContentView(currentLayoutResId);

        TextView roleLabel = findViewById(R.id.roleLabel);
        TextInputEditText usernameInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        TextInputEditText emailInput = findViewById(R.id.emailInput);
        Button registerBTN = findViewById(R.id.registerBTN);
        TextView loginLink = findViewById(R.id.loginLink);

        roleLabel.setText("seller".equals(role) ? "Registrierung für Verkäufer" : "Registrierung für Kunden");
        usernameInput.setHint("seller".equals(role) ? "Geschäftsname" : "Benutzername");

        registerBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String email = emailInput.getText().toString();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedUsername = SecureData.hashDataViaSHA(username);
            String hashedEmail = SecureData.hashDataViaSHA(email);

            if ("seller".equals(role)) {
                if (db.sellerExists(hashedUsername, hashedEmail)) {
                    Toast.makeText(this, "Geschäftsname oder Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.insertSeller(hashedUsername, hashedEmail, SecureData.hashPasswordViaBCrypt(password));
            } else {
                if (db.customerExists(hashedUsername, hashedEmail)) {
                    Toast.makeText(this, "Benutzername oder Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.insertCustomer(hashedUsername, hashedEmail, SecureData.hashPasswordViaBCrypt(password));
            }

            Toast.makeText(this, "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();
            showLoginView();
        });

        loginLink.setOnClickListener(view -> showLoginView());
    }

    private void showLoginView() {
        currentLayoutResId = R.layout.login_view;
        setContentView(currentLayoutResId);

        TextView loginTitle = findViewById(R.id.loginTitle);
        TextInputEditText usernameInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        Button loginBTN = findViewById(R.id.loginBTN);
        TextView registerLink = findViewById(R.id.registerLink);

        loginTitle.setText("seller".equals(role) ? "Login für Verkäufer" : "Login für Kunden");
        usernameInput.setHint("seller".equals(role) ? "Geschäftsname" : "Benutzername");

        loginBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedUsername = SecureData.hashDataViaSHA(username);
            String passwordHash;

            if ("seller".equals(role)) {
                passwordHash = db.getSellerPasswordHash(hashedUsername);
            } else {
                passwordHash = db.getCustomerPasswordHash(hashedUsername);
            }

            if (passwordHash != null && BCrypt.checkpw(password, passwordHash)) {
                loginSuccess(role);
            } else {
                Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
            }
        });

        registerLink.setOnClickListener(view -> showRegistrationView());
    }

    private void loginSuccess(String role) {
        Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
        SessionManager.saveLogin(this, role);
        Intent intent = new Intent(this, "seller".equals(role) ? SellerUIController.class : CustomerUIController.class);
        startActivity(intent);
        finish();
    }

    public boolean isRegistrationLayoutVisible() {
        return currentLayoutResId == R.layout.registration_view;
    }

    public boolean isLoginLayoutVisible() {
        return currentLayoutResId == R.layout.login_view;
    }
}
