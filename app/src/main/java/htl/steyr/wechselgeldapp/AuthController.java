package htl.steyr.wechselgeldapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import htl.steyr.wechselgeldapp.Database.AppDatabase;
import htl.steyr.wechselgeldapp.Database.AppDatabaseInstance;
import htl.steyr.wechselgeldapp.Database.Entity.Customer;
import htl.steyr.wechselgeldapp.Database.Entity.Seller;

import org.mindrot.jbcrypt.BCrypt;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthController extends Activity {

    private int currentLayoutResId;
    private String role;
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
        } else {
            roleLabel.setText("Registrierung für Kunden");
            usernameInput.setHint("Benutzername");
        }

        registerBTN.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String email = emailInput.getText().toString();

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

            if ("seller".equals(role)) {
                Seller seller = new Seller();
                seller.shopName = hashDataViaSHA(username);
                seller.email = hashDataViaSHA(email);
                seller.passwordHash = hashPasswordViaBCrypt(password);
                db.sellerDao().insert(seller);
            } else {
                Customer customer = new Customer();
                customer.displayName = hashDataViaSHA(username);
                customer.email = hashDataViaSHA(email);
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
                Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = false;

            if ("seller".equals(role)) {
                Seller seller = db.sellerDao().findByShopName(username);
                if (seller != null && BCrypt.checkpw(password, seller.passwordHash)) {
                    success = true;
                }

            } else {
                Customer customer = db.customerDao().findByDisplayName(username);
                if (customer != null && BCrypt.checkpw(password, customer.passwordHash)) {
                    success = true;
                }
            }

            if (success) {
                Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
                // Weiterleitung zur Hauptansicht etc.
            } else {
                Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
            }
        });

        registerLink.setOnClickListener(view -> showRegistrationView());
    }

    public boolean isRegistrationLayoutVisible() {
        return currentLayoutResId == R.layout.registration_view;
    }

    public boolean isLoginLayoutVisible() {
        return currentLayoutResId == R.layout.login_view;
    }

    public String hashPasswordViaBCrypt(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public String hashDataViaSHA(String data){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }}
