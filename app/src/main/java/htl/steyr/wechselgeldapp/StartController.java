package htl.steyr.wechselgeldapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


public class StartController extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_view);


        Button customerBTN = findViewById(R.id.customerBTN);
        Button sellerBTN = findViewById(R.id.sellerBTN);

        // Set button listeners to go to login/register screens for customer or seller
        customerBTN.setOnClickListener(view -> navigateTo("customer"));
        sellerBTN.setOnClickListener(view -> navigateTo("seller"));
    }


    // Navigate to AuthController with the selected role (customer or seller)
    private void navigateTo(String role) {
        Intent intent = new Intent(StartController.this, AuthController.class);
        intent.putExtra("user_role", role);
        startActivity(intent);
    }


}