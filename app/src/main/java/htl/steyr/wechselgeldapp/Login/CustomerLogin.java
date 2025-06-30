package htl.steyr.wechselgeldapp.Login;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import htl.steyr.wechselgeldapp.R;

public class CustomerLogin extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_view);

        TextView loginTitle = findViewById(R.id.loginTitle);
        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button loginBTN = findViewById(R.id.loginBTN);

        loginTitle.setText("Customer Login");

        loginBTN.setOnClickListener(view -> {
            // Customer-specific login logic
        });
    }
}
