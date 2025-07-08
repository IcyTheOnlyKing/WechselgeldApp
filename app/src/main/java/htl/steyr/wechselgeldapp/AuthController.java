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

    private int currentLayoutResId; // Keeps track of the current visible layout resource ID
    private String role; // User role: either "seller" or "customer"
    private DatabaseHelper db; // Database helper for database operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(getApplicationContext()); // Initialize database helper

        role = getIntent().getStringExtra("user_role"); // Get user role from intent extras
        showRegistrationView(); // Start by showing the registration screen
    }

    /**
     * Display the registration screen based on the user role.
     */
    private void showRegistrationView() {
        currentLayoutResId = R.layout.registration_view;
        setContentView(currentLayoutResId);

        // Find UI components from the layout
        TextView roleLabel = findViewById(R.id.roleLabel);
        TextInputEditText usernameInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        TextInputEditText emailInput = findViewById(R.id.emailInput);
        Button registerBTN = findViewById(R.id.registerBTN);
        TextView loginLink = findViewById(R.id.loginLink);

        // Set role-specific labels and hints
        roleLabel.setText("seller".equals(role) ? "Registration for Sellers" : "Registration for Customers");
        usernameInput.setHint("seller".equals(role) ? "Business Name" : "Username");

        // Handle registration button click
        registerBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String email = emailInput.getText().toString();

            // Check if any field is empty
            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hash username and email for security before storing/checking
            String hashedUsername = SecureData.hashDataViaSHA(username);
            String hashedEmail = SecureData.hashDataViaSHA(email);

            if ("seller".equals(role)) {
                // Check if seller with the same username/email already exists
                if (db.sellerExists(hashedUsername, hashedEmail)) {
                    Toast.makeText(this, "Business name or email already taken!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Insert new seller with hashed password
                db.insertSeller(hashedUsername, hashedEmail, SecureData.hashPasswordViaBCrypt(password));
            } else {
                // Check if customer with the same username/email already exists
                if (db.customerExists(hashedUsername, hashedEmail)) {
                    Toast.makeText(this, "Username or email already taken!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Insert new customer with hashed password
                db.insertCustomer(hashedUsername, hashedEmail, SecureData.hashPasswordViaBCrypt(password));
            }

            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            showLoginView(); // Switch to login view after successful registration
        });

        // Handle click on the login link to switch to login view
        loginLink.setOnClickListener(view -> showLoginView());
    }

    /**
     * Display the login screen based on the user role.
     */
    private void showLoginView() {
        currentLayoutResId = R.layout.login_view;
        setContentView(currentLayoutResId);

        // Find UI components from the layout
        TextView loginTitle = findViewById(R.id.loginTitle);
        TextInputEditText usernameInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        Button loginBTN = findViewById(R.id.loginBTN);
        TextView registerLink = findViewById(R.id.registerLink);

        // Set role-specific labels and hints
        loginTitle.setText("seller".equals(role) ? "Login for Sellers" : "Login for Customers");
        usernameInput.setHint("seller".equals(role) ? "Business Name" : "Username");

        // Handle login button click
        loginBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            // Check if fields are empty
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hash username to match stored database entries
            String hashedUsername = SecureData.hashDataViaSHA(username);
            String passwordHash;

            // Retrieve stored password hash based on role
            if ("seller".equals(role)) {
                passwordHash = db.getSellerPasswordHash(hashedUsername);
            } else {
                passwordHash = db.getCustomerPasswordHash(hashedUsername);
            }

            // Verify entered password against stored hash using BCrypt
            if (passwordHash != null && BCrypt.checkpw(password, passwordHash)) {
                loginSuccess(role); // Successful login
            } else {
                Toast.makeText(this, "Invalid login credentials!", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle click on the registration link to switch to registration view
        registerLink.setOnClickListener(view -> showRegistrationView());
    }

    /**
     * Handle successful login: save session and start appropriate UI.
     *
     * @param role The user role ("seller" or "customer")
     */
    private void loginSuccess(String role) {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
        SessionManager.saveLogin(this, role); // Save login session

        // Start Seller or Customer UI based on role
        Intent intent = new Intent(this, "seller".equals(role) ? SellerUIController.class : CustomerUIController.class);
        startActivity(intent);
        finish(); // Close current activity
    }

    /**
     * Check if registration layout is currently visible.
     */
    public boolean isRegistrationLayoutVisible() {
        return currentLayoutResId == R.layout.registration_view;
    }

    /**
     * Check if login layout is currently visible.
     */
    public boolean isLoginLayoutVisible() {
        return currentLayoutResId == R.layout.login_view;
    }
}
