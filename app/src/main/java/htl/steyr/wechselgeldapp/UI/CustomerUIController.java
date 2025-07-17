package htl.steyr.wechselgeldapp.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;

import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.StartController;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.ProfileFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.ConnectFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.HistoryFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.HomeFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.TransactionFragment;
import htl.steyr.wechselgeldapp.Utilities.Security.SessionManager;

public class CustomerUIController extends AppCompatActivity {
    private static final String TAG = "CustomerUIController";
    private TextView headerName;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.customer_ui);
            initializeViews();
            setupNavigation();
            loadFragment(new HomeFragment());
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Fehler beim Laden der Ansicht", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        try {
            headerName = findViewById(R.id.restaurant_name);
            if (headerName != null) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String displayName = prefs.getString("user_display_name", "Kunde");
                headerName.setText(displayName);
            }

            drawerLayout = findViewById(R.id.drawer_layout);
        } catch (Exception e) {
            Log.e(TAG, "Error in initializeViews: " + e.getMessage(), e);
        }
    }

    private void setupNavigation() {
        try {
            ImageButton menuButton = findViewById(R.id.menu_icon);
            if (menuButton != null) {
                menuButton.setOnClickListener(v -> {
                    if (drawerLayout != null) {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                });
            }

            ImageButton closeButton = findViewById(R.id.btn_close);
            if (closeButton != null) {
                closeButton.setOnClickListener(v -> {
                    if (drawerLayout != null) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                });
            }

            LinearLayout homeIcon = findViewById(R.id.homeIcon);
            LinearLayout connectIcon = findViewById(R.id.connectIcon);
            LinearLayout transactionIcon = findViewById(R.id.transactionIcon);
            LinearLayout historyIcon = findViewById(R.id.historyIcon);
            ImageView profileIcon = findViewById(R.id.profile_image);

            if (homeIcon != null) {
                homeIcon.setOnClickListener(v -> loadFragment(new HomeFragment()));
            }
            if (connectIcon != null) {
                connectIcon.setOnClickListener(v -> loadFragment(new ConnectFragment()));
            }
            if (transactionIcon != null) {
                transactionIcon.setOnClickListener(v -> loadFragment(new TransactionFragment()));
            }
            if (historyIcon != null) {
                historyIcon.setOnClickListener(v -> loadFragment(new HistoryFragment()));
            }
            if (profileIcon != null) {
                profileIcon.setOnClickListener(v -> loadFragment(new ProfileFragment()));
            }

            MaterialButton btnBackup = findViewById(R.id.btn_backup);
            MaterialButton btnUnpair = findViewById(R.id.btn_unpair);
            MaterialButton btnLogout = findViewById(R.id.btn_logout);

            if (btnBackup != null) {
                btnBackup.setOnClickListener(v -> {
                    Toast.makeText(this, "Backup wird erstellt...", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                });
            }

            if (btnUnpair != null) {
                btnUnpair.setOnClickListener(v -> {
                    Toast.makeText(this, "Kopplung wird getrennt...", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                });
            }

            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> {
                    Toast.makeText(this, "Abmeldung...", Toast.LENGTH_SHORT).show();
                    SessionManager.logout(this);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    startActivity(new Intent(this, StartController.class));
                    finish();
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in setupNavigation: " + e.getMessage(), e);
        }
    }

    private void loadFragment(Fragment fragment) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();

            if (fragment instanceof BaseFragment && headerName != null) {
                String title = ((BaseFragment) fragment).getTitle();
                headerName.setText(title);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment: " + e.getMessage(), e);
            Toast.makeText(this, "Fehler beim Laden des Fragments", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
