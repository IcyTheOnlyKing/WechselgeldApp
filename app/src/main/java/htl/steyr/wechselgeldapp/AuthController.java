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

    private int currentLayoutResId;
    private String role; // "seller" or "customer" role
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabaseInstance.getInstance(getApplicationContext());

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


        if ("seller".equals(role)) {
            roleLabel.setText("Registrierung für Verkäufer");
            usernameInput.setHint("Geschäftsname");
        } else if ("customer".equals(role)) {
            roleLabel.setText("Registrierung für Kunden");
            usernameInput.setHint("Benutzername");
        }

        registerBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String email = emailInput.getText().toString();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedUsername = hashDataViaSHA(username);
            String hashedEmail = hashDataViaSHA(email);

            if ("seller".equals(role)) {
                if (db.sellerDao().findByShopName(hashedUsername) != null) {
                    Toast.makeText(this, "Geschäftsname bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (db.sellerDao().findByEmail(hashedEmail) != null) {
                    Toast.makeText(this, "Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Seller seller = new Seller();
                seller.shopName = hashedUsername;
                seller.email = hashedEmail;
                seller.passwordHash = hashPasswordViaBCrypt(password);
                db.sellerDao().insert(seller);

            } else {
                if (db.customerDao().findByDisplayName(hashedUsername) != null) {
                    Toast.makeText(this, "Benutzername bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (db.customerDao().findByEmail(hashedEmail) != null) {
                    Toast.makeText(this, "Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Customer customer = new Customer();
                customer.displayName = hashedUsername;
                customer.email = hashedEmail;
                customer.passwordHash = hashPasswordViaBCrypt(password);
                db.customerDao().insert(customer);
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

        if ("seller".equals(role)) {
            loginTitle.setText("Login für Verkäufer");
            usernameInput.setHint("Geschäftsname");
        } else {
            loginTitle.setText("Login für Kunden");
            usernameInput.setHint("Benutzername");
        }

        loginBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedUsername = hashDataViaSHA(username);

            if ("seller".equals(role)) {
                Seller seller = db.sellerDao().findByShopName(hashedUsername);
                if (seller != null && BCrypt.checkpw(password, seller.passwordHash)) {
                    Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
                    saveLoginStatus("seller");
                    startActivity(new Intent(this, SellerUIController.class));
                    finish();
                } else {
                    Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Customer customer = db.customerDao().findByDisplayName(hashedUsername);
                if (customer != null && BCrypt.checkpw(password, customer.passwordHash)) {
                    Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
                    saveLoginStatus("customer");
                    startActivity(new Intent(this, CustomerUIController.class));
                    finish();
                } else {
                    Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerLink.setOnClickListener(view -> showRegistrationView());
    }

    // Speichert den Login-Zustand in SharedPreferences
    private void saveLoginStatus(String role) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_role", role)
                .apply();
    }

    public boolean isRegistrationLayoutVisible() {
        return currentLayoutResId == R.layout.registration_view;
    }

    public boolean isLoginLayoutVisible() {
        return currentLayoutResId == R.layout.login_view;
    }

    public String hashPasswordViaBCrypt(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

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
            throw new RuntimeException(e);
        }
    }
}
