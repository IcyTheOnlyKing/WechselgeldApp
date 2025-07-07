package htl.steyr.wechselgeldapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.UI.CustomerUIController;
import htl.steyr.wechselgeldapp.UI.SellerUIController;

import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

            String hashedUsername = hashDataViaSHA(username);
            String hashedEmail = hashDataViaSHA(email);

            if ("seller".equals(role)) {
                if (sellerExists(hashedUsername, hashedEmail)) {
                    Toast.makeText(this, "Geschäftsname oder Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.insertSeller(hashedUsername, hashedEmail, hashPasswordViaBCrypt(password));
            } else {
                if (customerExists(hashedUsername, hashedEmail)) {
                    Toast.makeText(this, "Benutzername oder Email bereits vergeben!", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.insertCustomer(hashedUsername, hashedEmail, hashPasswordViaBCrypt(password));
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

            String hashedUsername = hashDataViaSHA(username);

            if ("seller".equals(role)) {
                Cursor cursor = db.getReadableDatabase().rawQuery(
                        "SELECT passwordHash FROM Seller WHERE shopName = ?", new String[]{hashedUsername});
                if (cursor.moveToFirst()) {
                    String passwordHash = cursor.getString(0);
                    if (BCrypt.checkpw(password, passwordHash)) {
                        loginSuccess("seller");
                    } else {
                        Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            } else {
                Cursor cursor = db.getReadableDatabase().rawQuery(
                        "SELECT passwordHash FROM Customer WHERE displayName = ?", new String[]{hashedUsername});
                if (cursor.moveToFirst()) {
                    String passwordHash = cursor.getString(0);
                    if (BCrypt.checkpw(password, passwordHash)) {
                        loginSuccess("customer");
                    } else {
                        Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Ungültige Login-Daten!", Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            }
        });

        registerLink.setOnClickListener(view -> showRegistrationView());
    }

    private boolean sellerExists(String shopName, String email) {
        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT id FROM Seller WHERE shopName = ? OR email = ?", new String[]{shopName, email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private boolean customerExists(String displayName, String email) {
        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT id FROM Customer WHERE displayName = ? OR email = ?", new String[]{displayName, email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private void loginSuccess(String role) {
        Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
        saveLoginStatus(role);
        Intent intent = new Intent(this, "seller".equals(role) ? SellerUIController.class : CustomerUIController.class);
        startActivity(intent);
        finish();
    }

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
