package htl.steyr.wechselgeldapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private int currentLayoutResId; // saves which screen is shown
    private String role; // "seller" or "customer"
    private AppDatabase db; // database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabaseInstance.getInstance(getApplicationContext()); // get database

        role = getIntent().getStringExtra("user_role"); // get role from previous screen
        showRegistrationView(); // show registration screen first
    }

    private void showRegistrationView() {
        currentLayoutResId = R.layout.registration_view;
        setContentView(currentLayoutResId); // show registration layout

        // find UI elements
        TextView roleLabel = findViewById(R.id.roleLabel);
        TextInputEditText usernameInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        TextInputEditText emailInput = findViewById(R.id.emailInput);
        Button registerBTN = findViewById(R.id.registerBTN);
        TextView loginLink = findViewById(R.id.loginLink);

        // different text for customer or seller
        if ("seller".equals(role)) {
            roleLabel.setText("Registrierung für Verkäufer"); // seller registration
            usernameInput.setHint("Geschäftsname"); // business name
        } else {
            roleLabel.setText("Registrierung für Kunden"); // customer registration
            usernameInput.setHint("Benutzername"); // username
        }

        // when register button is clicked
        registerBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String email = emailInput.getText().toString();

            // check if all fields are filled
            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedUsername = hashDataViaSHA(username); // secure username
            String hashedEmail = hashDataViaSHA(email); // secure email

            if ("seller".equals(role)) {
                // check if business name or email is already used
                if (db.sellerDao().findByShopName(hashedUsername) != null) {
                    Toast.makeText(this, "Geschäftsname bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (db.sellerDao().findByEmail(hashedEmail) != null) {
                    Toast.makeText(this, "Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // create new seller and save to database
                Seller seller = new Seller();
                seller.shopName = hashedUsername;
                seller.email = hashedEmail;
                seller.passwordHash = hashPasswordViaBCrypt(password); // secure password
                db.sellerDao().insert(seller);

            } else {
                // check if username or email is already used
                if (db.customerDao().findByDisplayName(hashedUsername) != null) {
                    Toast.makeText(this, "Benutzername bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (db.customerDao().findByEmail(hashedEmail) != null) {
                    Toast.makeText(this, "Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // create new customer and save to database
                Customer customer = new Customer();
                customer.displayName = hashedUsername;
                customer.email = hashedEmail;
                customer.passwordHash = hashPasswordViaBCrypt(password); // secure password
                db.customerDao().insert(customer);
            }

            Toast.makeText(this, "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();
            showLoginView(); // go to login screen
        });

        loginLink.setOnClickListener(view -> showLoginView()); // go to login screen if clicked
    }

    private void showLoginView() {
        currentLayoutResId = R.layout.login_view;
        setContentView(currentLayoutResId); // show login layout

        // find UI elements
        TextView loginTitle = findViewById(R.id.loginTitle);
        TextInputEditText usernameInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        Button loginBTN = findViewById(R.id.loginBTN);
        TextView registerLink = findViewById(R.id.registerLink);

        // different text for customer or seller
        if ("seller".equals(role)) {
            loginTitle.setText("Login für Verkäufer");
            usernameInput.setHint("Geschäftsname");
        } else {
            loginTitle.setText("Login für Kunden");
            usernameInput.setHint("Benutzername");
        }

        // when login button is clicked
        loginBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            // check if both fields are filled
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedUsername = hashDataViaSHA(username); // secure username

            if ("seller".equals(role)) {
                Seller seller = db.sellerDao().findByShopName(hashedUsername);
                if (seller != null && BCrypt.checkpw(password, seller.passwordHash)) {
                    // correct login
                    Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
                    saveLoginStatus("seller"); // save login in SharedPreferences
                    startActivity(new Intent(this, SellerUIController.class)); // go to seller screen
                    finish();
                } else {
                    // wrong login
                    Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Customer customer = db.customerDao().findByDisplayName(hashedUsername);
                if (customer != null && BCrypt.checkpw(password, customer.passwordHash)) {
                    // correct login
                    Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
                    saveLoginStatus("customer"); // save login in SharedPreferences
                    startActivity(new Intent(this, CustomerUIController.class)); // go to customer screen
                    finish();
                } else {
                    // wrong login
                    Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerLink.setOnClickListener(view -> showRegistrationView()); // back to registration
    }

    // save that the user is logged in and save the role
    private void saveLoginStatus(String role) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_logged_in", true) // remember that user is logged in
                .putString("user_role", role)     // save if customer or seller
                .apply();
    }

    // check which screen is shown
    public boolean isRegistrationLayoutVisible() {
        return currentLayoutResId == R.layout.registration_view;
    }

    public boolean isLoginLayoutVisible() {
        return currentLayoutResId == R.layout.login_view;
    }

    // password hashing with bcrypt for security
    public String hashPasswordViaBCrypt(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // secure data hashing with SHA-256 (for username and email)
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
            throw new RuntimeException(e); // should not happen
        }
    }
}
