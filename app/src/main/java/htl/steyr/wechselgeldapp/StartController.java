package htl.steyr.wechselgeldapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

public class StartController extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is already logged in
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false); // true = logged in
        String role = prefs.getString("user_role", ""); // get saved role (seller or customer)

        if (isLoggedIn && role != null) {
            // User is already logged in, send him to the correct screen
            if (role.equals("seller")) {
                startActivity(new Intent(this, htl.steyr.wechselgeldapp.UI.SellerUIController.class));
            } else if (role.equals("customer")) {
                startActivity(new Intent(this, htl.steyr.wechselgeldapp.UI.CustomerUIController.class));
            }
            finish(); // close this screen so user can't go back here
            return;
        }

        // Show start screen with buttons if user is not logged in
        setContentView(R.layout.start_view);

        Button customerBTN = findViewById(R.id.customerBTN); // Button for customer role
        Button sellerBTN = findViewById(R.id.sellerBTN);     // Button for seller role

        // When customer button is clicked -> go to login/register with customer role
        customerBTN.setOnClickListener(view -> navigateTo("customer"));
        // When seller button is clicked -> go to login/register with seller role
        sellerBTN.setOnClickListener(view -> navigateTo("seller"));
    }

    // Go to login/register screen and tell it the selected role
    private void navigateTo(String role) {
        Intent intent = new Intent(StartController.this, AuthController.class);
        intent.putExtra("user_role", role); // send role to next screen
        startActivity(intent); // open next screen
    }
}