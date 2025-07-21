package htl.steyr.wechselgeldapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.material.textfield.TextInputEditText;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.UI.CustomerUIController;
import htl.steyr.wechselgeldapp.UI.SellerUIController;
import htl.steyr.wechselgeldapp.Utilities.Security.SecureData;
import htl.steyr.wechselgeldapp.Utilities.Security.SessionManager;

import org.mindrot.jbcrypt.BCrypt;

public class AuthController extends Activity {

    private int currentLayoutResId;
    private String role;
    private DatabaseHelper db;

    // Registration Views
    private TextView roleLabel;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private TextInputEditText emailInput;
    private Button registerBTN;
    private TextView loginLink;

    // Login Views
    private TextView loginTitle;
    private TextInputEditText loginUsernameInput;
    private TextInputEditText loginPasswordInput;
    private Button loginBTN;
    private TextView registerLink;

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
        initRegistrationView();

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

            String displayName = username; // wird jetzt im Klartext gespeichert
            String hashedEmail = SecureData.hashDataViaSHA(email);
            String hashedPassword = SecureData.hashPasswordViaBCrypt(password);

            if ("seller".equals(role)) {
                if (db.sellerExists(displayName, hashedEmail)) {
                    Toast.makeText(this, "Geschäftsname oder Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.insertSeller(displayName, hashedEmail, hashedPassword);
            } else {
                if (db.customerExists(displayName, hashedEmail)) {
                    Toast.makeText(this, "Benutzername oder Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.insertCustomer(displayName, hashedEmail, hashedPassword);
            }

            Toast.makeText(this, "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();
            showLoginView();
        });

        loginLink.setOnClickListener(view -> showLoginView());
    }

    private void showLoginView() {
        currentLayoutResId = R.layout.login_view;
        setContentView(currentLayoutResId);
        initLoginView();

        loginTitle.setText("seller".equals(role) ? "Login für Verkäufer" : "Login für Kunden");
        loginUsernameInput.setHint("seller".equals(role) ? "Geschäftsname" : "Benutzername");

        loginBTN.setOnClickListener(view -> {
            String username = loginUsernameInput.getText().toString().trim();
            String password = loginPasswordInput.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Für Testdaten: Direkte Passwortprüfung für Admin
            if ("admin".equals(username) && "admin".equals(password)) {
                loginSuccess(role, username);
                return;
            }

            String passwordHash;

            if ("seller".equals(role)) {
                passwordHash = db.getSellerPasswordHash(username);
            } else {
                passwordHash = db.getCustomerPasswordHash(username);
            }

            if (passwordHash != null && BCrypt.checkpw(password, passwordHash)) {
                loginSuccess(role, username); // Weiter mit displayName
            } else {
                Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
            }
        });

        registerLink.setOnClickListener(view -> showRegistrationView());
    }

    /**
     * Called when login is successful.
     */
    private void loginSuccess(String role, String displayName) {
        Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();

        // Save session with role and displayName
        getSharedPreferences("user_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_logged_in", true)
                .putString("user_role", role)
                .putString("user_display_name", displayName)
                .apply();

        // Create intent for the appropriate UI controller
        Intent intent;
        if ("seller".equals(role)) {
            intent = new Intent(this, SellerUIController.class);
        } else {
            intent = new Intent(this, CustomerUIController.class);
        }

        // Clear the entire activity stack and start fresh
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // No need to call finish() since we're clearing the task
    }

    private void initRegistrationView() {
        roleLabel = findViewById(R.id.roleLabel);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        emailInput = findViewById(R.id.emailInput);
        registerBTN = findViewById(R.id.registerBTN);
        loginLink = findViewById(R.id.loginLink);
    }

    private void initLoginView() {
        loginTitle = findViewById(R.id.loginTitle);
        loginUsernameInput = findViewById(R.id.usernameInput);
        loginPasswordInput = findViewById(R.id.passwordInput);
        loginBTN = findViewById(R.id.loginBTN);
        registerLink = findViewById(R.id.registerLink);
    }

    public boolean isRegistrationLayoutVisible() {
        return currentLayoutResId == R.layout.registration_view;
    }

    public boolean isLoginLayoutVisible() {
        return currentLayoutResId == R.layout.login_view;
    }
}