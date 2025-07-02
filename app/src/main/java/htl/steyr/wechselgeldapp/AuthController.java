package htl.steyr.wechselgeldapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import htl.steyr.wechselgeldapp.Database.AppDatabase;
import htl.steyr.wechselgeldapp.Database.AppDatabaseInstance;
import htl.steyr.wechselgeldapp.Database.Entity.Customer;
import htl.steyr.wechselgeldapp.Database.Entity.Seller;

import htl.steyr.wechselgeldapp.UI.CustomerUIController;
import htl.steyr.wechselgeldapp.UI.SellerUIController;

import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthController extends Activity {

    private int currentLayoutResId; // Stores which layout is currently visible
    private String role; // "seller" or "customer"
    private AppDatabase db; // Database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabaseInstance.getInstance(getApplicationContext()); // Get database

        role = getIntent().getStringExtra("user_role"); // Get role from previous screen
        showRegistrationView(); // Show registration screen by default
    }

    private void showRegistrationView() {
        currentLayoutResId = R.layout.registration_view;
        setContentView(currentLayoutResId); // Show registration layout

        // Find UI elements
        TextView roleLabel = findViewById(R.id.roleLabel);
        TextInputEditText usernameInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        TextInputEditText emailInput = findViewById(R.id.emailInput);
        Button registerBTN = findViewById(R.id.registerBTN);
        TextView loginLink = findViewById(R.id.loginLink);

        // Adjust UI text depending on role
        if ("seller".equals(role)) {
            roleLabel.setText("Registrierung für Verkäufer");
            usernameInput.setHint("Geschäftsname");
        } else {
            roleLabel.setText("Registrierung für Kunden");
            usernameInput.setHint("Benutzername");
        }

        // Register button click listener
        registerBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String email = emailInput.getText().toString();

            // Input validation
            if (username.isEmpty()) {
                Toast.makeText(this, "Bitte einen Benutzernamen eingeben!", Toast.LENGTH_SHORT).show();
                return;
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Bitte ein Passwort eingeben!", Toast.LENGTH_SHORT).show();
                return;
            } else if (email.isEmpty()) {
                Toast.makeText(this, "Bitte eine Email eingeben!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hash username and email for privacy
            String hashedUsername = hashDataViaSHA(username);
            String hashedEmail = hashDataViaSHA(email);

            if ("seller".equals(role)) {
                // Check if seller already exists by shop name or email
                Seller existingSellerByName = db.sellerDao().findByShopName(hashedUsername);
                Seller existingSellerByEmail = db.sellerDao().findByEmail(hashedEmail);
                if (existingSellerByName != null) {
                    Toast.makeText(this, "Geschäftsname bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (existingSellerByEmail != null) {
                    Toast.makeText(this, "Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create new seller and save to database
                Seller seller = new Seller();
                seller.shopName = hashedUsername;
                seller.email = hashedEmail;
                seller.passwordHash = hashPasswordViaBCrypt(password);
                db.sellerDao().insert(seller);

            } else {
                // Check if customer already exists by username or email
                Customer existingCustomerByName = db.customerDao().findByDisplayName(hashedUsername);
                Customer existingCustomerByEmail = db.customerDao().findByEmail(hashedEmail);
                if (existingCustomerByName != null) {
                    Toast.makeText(this, "Benutzername bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (existingCustomerByEmail != null) {
                    Toast.makeText(this, "Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create new customer and save to database
                Customer customer = new Customer();
                customer.displayName = hashedUsername;
                customer.email = hashedEmail;
                customer.passwordHash = hashPasswordViaBCrypt(password);
                db.customerDao().insert(customer);
            }

            Toast.makeText(this, "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();
            showLoginView(); // Switch to login screen
        });

        loginLink.setOnClickListener(view -> showLoginView()); // Link to login screen
    }

    private void showLoginView() {
        currentLayoutResId = R.layout.login_view;
        setContentView(currentLayoutResId); // Show login layout

        // Find UI elements
        TextView loginTitle = findViewById(R.id.loginTitle);
        TextInputEditText usernameInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        Button loginBTN = findViewById(R.id.loginBTN);
        TextView registerLink = findViewById(R.id.registerLink);

        // Adjust UI text depending on role
        if ("seller".equals(role)) {
            loginTitle.setText("Login für Verkäufer");
            usernameInput.setHint("Geschäftsname");
        } else {
            loginTitle.setText("Login für Kunden");
            usernameInput.setHint("Benutzername");
        }

        // Login button click listener
        loginBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("seller".equals(role)) {
                // Check seller login
                Seller seller = db.sellerDao().findByShopName(hashDataViaSHA(username));
                if (seller != null && BCrypt.checkpw(password, seller.passwordHash)) {
                    Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, SellerUIController.class); // Start seller UI
                    startActivity(intent);

                } else {
                    Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Check customer login
                Customer customer = db.customerDao().findByDisplayName(hashDataViaSHA(username));
                if (customer != null && BCrypt.checkpw(password, customer.passwordHash)) {
                    Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, CustomerUIController.class); // Start customer UI
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerLink.setOnClickListener(view -> showRegistrationView()); // Link to registration screen
    }

    // Returns true if registration layout is visible
    public boolean isRegistrationLayoutVisible() {
        return currentLayoutResId == R.layout.registration_view;
    }

    // Returns true if login layout is visible
    public boolean isLoginLayoutVisible() {
        return currentLayoutResId == R.layout.login_view;
    }

    // Hash password using BCrypt (for secure storage)
    public String hashPasswordViaBCrypt(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Hash general data using SHA-256 (e.g., username, email)
    public String hashDataViaSHA(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // Should never happen
        }
    }
}
