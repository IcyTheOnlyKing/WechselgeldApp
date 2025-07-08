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

    private int currentLayoutResId; // Stores the current layout resource ID to track which view is active
    private String role; // Can be "seller" or "customer" depending on the user role
    private DatabaseHelper db; // Database helper for all database operations

    // Views for Registration
    private TextView roleLabel;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private TextInputEditText emailInput;
    private Button registerBTN;
    private TextView loginLink;

    // Views for Login
    private TextView loginTitle;
    private TextInputEditText loginUsernameInput;
    private TextInputEditText loginPasswordInput;
    private Button loginBTN;
    private TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(getApplicationContext());

        // Retrieve role passed via Intent
        role = getIntent().getStringExtra("user_role");
        showRegistrationView();
    }

    /**
     * Displays the registration view and initializes the inputs and buttons.
     */
    private void showRegistrationView() {
        currentLayoutResId = R.layout.registration_view;
        setContentView(currentLayoutResId);

        initRegistrationView();

        // Update label and hints depending on role
        roleLabel.setText("seller".equals(role) ? "Registrierung für Verkäufer" : "Registrierung für Kunden");
        usernameInput.setHint("seller".equals(role) ? "Geschäftsname" : "Benutzername");

        // Handle registration button click
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

        // Switch to login view
        loginLink.setOnClickListener(view -> showLoginView());
    }

    /**
     * Displays the login view and initializes the inputs and buttons.
     */
    private void showLoginView() {
        currentLayoutResId = R.layout.login_view;
        setContentView(currentLayoutResId);

        initLoginView();

        // Update title and hints based on role
        loginTitle.setText("seller".equals(role) ? "Login für Verkäufer" : "Login für Kunden");
        loginUsernameInput.setHint("seller".equals(role) ? "Geschäftsname" : "Benutzername");

        // Handle login button click
        loginBTN.setOnClickListener(view -> {
            String username = loginUsernameInput.getText().toString().trim();
            String password = loginPasswordInput.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedUsername = SecureData.hashDataViaSHA(username);
            String passwordHash;

            // Get stored password hash from database based on role
            if ("seller".equals(role)) {
                passwordHash = db.getSellerPasswordHash(hashedUsername);
            } else {
                passwordHash = db.getCustomerPasswordHash(hashedUsername);
            }

            // Check password with BCrypt
            if (passwordHash != null && BCrypt.checkpw(password, passwordHash)) {
                loginSuccess(role);
            } else {
                Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
            }
        });

        // Switch to registration view
        registerLink.setOnClickListener(view -> showRegistrationView());
    }

    /**
     * Called on successful login, opens the correct UI and saves session.
     * @param role Either "seller" or "customer"
     */
    private void loginSuccess(String role) {
        Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
        SessionManager.saveLogin(this, role);

        // Open appropriate main UI
        Intent intent = new Intent(this, "seller".equals(role) ? SellerUIController.class : CustomerUIController.class);
        startActivity(intent);
        finish(); // Prevent going back to login screen
    }

    /**
     * Initializes all views for the registration screen.
     */
    private void initRegistrationView() {
        roleLabel = findViewById(R.id.roleLabel);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        emailInput = findViewById(R.id.emailInput);
        registerBTN = findViewById(R.id.registerBTN);
        loginLink = findViewById(R.id.loginLink);
    }

    /**
     * Initializes all views for the login screen.
     */
    private void initLoginView() {
        loginTitle = findViewById(R.id.loginTitle);
        loginUsernameInput = findViewById(R.id.usernameInput);
        loginPasswordInput = findViewById(R.id.passwordInput);
        loginBTN = findViewById(R.id.loginBTN);
        registerLink = findViewById(R.id.registerLink);
    }

    /**
     * @return True if the registration layout is currently visible
     */
    public boolean isRegistrationLayoutVisible() {
        return currentLayoutResId == R.layout.registration_view;
    }

    /**
     * @return True if the login layout is currently visible
     */
    public boolean isLoginLayoutVisible() {
        return currentLayoutResId == R.layout.login_view;
    }
}
