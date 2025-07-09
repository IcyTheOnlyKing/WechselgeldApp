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
import htl.steyr.wechselgeldapp.Utilities.ScreenChanger;

import org.mindrot.jbcrypt.BCrypt;

public class AuthController extends Activity {

    private int currentLayoutResId; // Stores the current layout resource ID to track which view is active
    private String role; // "seller" or "customer"
    private DatabaseHelper db; // Database helper to access and modify user data

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

        // Get user role from intent: "seller" or "customer"
        role = getIntent().getStringExtra("user_role");

        // Show the registration screen first
        showRegistrationView();
    }

    /**
     * Shows the registration screen and sets up all views and logic.
     */
    private void showRegistrationView() {
        currentLayoutResId = R.layout.registration_view;
        setContentView(currentLayoutResId);
        initRegistrationView();

        // Change the label text and hint based on the user role
        roleLabel.setText("seller".equals(role) ? "Registrierung für Verkäufer" : "Registrierung für Kunden");
        usernameInput.setHint("seller".equals(role) ? "Geschäftsname" : "Benutzername");

        // Register button logic
        registerBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String email = emailInput.getText().toString();

            // Make sure no fields are empty
            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hash username and email using SHA-256 for safe storage
            String hashedUsername = SecureData.hashDataViaSHA(username);
            String hashedEmail = SecureData.hashDataViaSHA(email);

            // Register seller or customer based on role
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

            // Success message and go to login screen
            Toast.makeText(this, "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();
            showLoginView();
        });

        // Switch to login screen when link is clicked
        loginLink.setOnClickListener(view -> showLoginView());
    }

    /**
     * Shows the login screen and sets up the login logic.
     */
    private void showLoginView() {
        currentLayoutResId = R.layout.login_view;
        setContentView(currentLayoutResId);
        initLoginView();

        // Set dynamic title and hint based on user role
        loginTitle.setText("seller".equals(role) ? "Login für Verkäufer" : "Login für Kunden");
        loginUsernameInput.setHint("seller".equals(role) ? "Geschäftsname" : "Benutzername");

        // Login button logic
        loginBTN.setOnClickListener(view -> {
            String username = loginUsernameInput.getText().toString().trim();
            String password = loginPasswordInput.getText().toString();

            // Check if any field is empty
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedUsername = SecureData.hashDataViaSHA(username);
            String passwordHash;

            // Fetch the stored password hash for comparison
            if ("seller".equals(role)) {
                passwordHash = db.getSellerPasswordHash(hashedUsername);
            } else {
                passwordHash = db.getCustomerPasswordHash(hashedUsername);
            }

            // Verify password using BCrypt
            if (passwordHash != null && BCrypt.checkpw(password, passwordHash)) {
                loginSuccess(role); // Proceed if valid
            } else {
                Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
            }
        });

        // Switch to registration screen
        registerLink.setOnClickListener(view -> showRegistrationView());
    }

    /**
     * Called when login is successful.
     * Navigates to the correct UI based on the user role.
     *
     * @param role "seller" or "customer"
     */
    private void loginSuccess(String role) {
        Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();

        // Save user session so they stay logged in
        SessionManager.saveLogin(this, role);

        // Navigate to the correct dashboard using ScreenChanger utility class
        if ("seller".equals(role)) {
            ScreenChanger.changeScreen(this, SellerUIController.class);
        } else {
            ScreenChanger.changeScreen(this, CustomerUIController.class);
        }

        // Close the AuthController so the user can't go back to login
        finish();
    }

    /**
     * Finds and assigns all views for the registration screen.
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
     * Finds and assigns all views for the login screen.
     */
    private void initLoginView() {
        loginTitle = findViewById(R.id.loginTitle);
        loginUsernameInput = findViewById(R.id.usernameInput);
        loginPasswordInput = findViewById(R.id.passwordInput);
        loginBTN = findViewById(R.id.loginBTN);
        registerLink = findViewById(R.id.registerLink);
    }

    /**
     * @return true if the current screen is the registration view
     */
    public boolean isRegistrationLayoutVisible() {
        return currentLayoutResId == R.layout.registration_view;
    }

    /**
     * @return true if the current screen is the login view
     */
    public boolean isLoginLayoutVisible() {
        return currentLayoutResId == R.layout.login_view;
    }
}
