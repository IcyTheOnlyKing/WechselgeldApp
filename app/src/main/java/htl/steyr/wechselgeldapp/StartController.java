package htl.steyr.wechselgeldapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import htl.steyr.wechselgeldapp.Authentication.AuthController;

public class StartController extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_view);

        Button customerBTN = findViewById(R.id.customerBTN);
        Button sellerBTN = findViewById(R.id.sellerBTN);

        // Navigate to Sign in or Sign up screens
        customerBTN.setOnClickListener(view -> navigateTo());
        sellerBTN.setOnClickListener(view -> navigateTo());
    }

    private void navigateTo() {
        Intent intent = new Intent(StartController.this, AuthController.class);
        startActivity(intent);
    }
}
