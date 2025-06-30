package htl.steyr.wechselgeldapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import htl.steyr.wechselgeldapp.Login.CustomerLogin;
import htl.steyr.wechselgeldapp.Login.SellerLogin;

public class StartController extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_view);

        Button customerBTN = findViewById(R.id.customerBTN);
        Button sellerBTN = findViewById(R.id.sellerBTN);

        // Navigate to login screens
        customerBTN.setOnClickListener(view -> navigateTo(CustomerLogin.class));
        sellerBTN.setOnClickListener(view -> navigateTo(SellerLogin.class));
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(StartController.this, targetActivity);
        startActivity(intent);
    }
}
