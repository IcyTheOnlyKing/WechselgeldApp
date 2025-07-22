package htl.steyr.wechselgeldapp.UI;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;

import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
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
    private Fragment currentFragment;

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_ui);

        initializeViews();
        setupNavigation();
        loadFragment(new HomeFragment());
    }

    private void initializeViews() {
        headerName = findViewById(R.id.restaurant_name);
        drawerLayout = findViewById(R.id.drawer_layout);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String displayName = prefs.getString("user_display_name", "Customer");

        if (headerName != null) {
            headerName.setText(displayName);
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT})
    private void setupNavigation() {
        ImageButton menuButton = findViewById(R.id.menu_icon);
        ImageButton closeButton = findViewById(R.id.btn_close);

        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            });
        }

        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        findViewById(R.id.homeIcon).setOnClickListener(v -> loadFragment(new HomeFragment()));
        findViewById(R.id.connectIcon).setOnClickListener(v -> loadFragment(new ConnectFragment()));
        findViewById(R.id.transactionIcon).setOnClickListener(v -> loadFragment(new TransactionFragment()));
        findViewById(R.id.historyIcon).setOnClickListener(v -> loadFragment(new HistoryFragment()));
        findViewById(R.id.profile_image).setOnClickListener(v -> loadFragment(new ProfileFragment()));

        MaterialButton btnBackup = findViewById(R.id.btn_backup);
        MaterialButton btnUnpair = findViewById(R.id.btn_unpair);
        MaterialButton btnLogout = findViewById(R.id.btn_logout);

        if (btnBackup != null) {
            btnBackup.setOnClickListener(v -> {
                Toast.makeText(this, "Creating backup...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                // TODO: Backup implementieren
            });
        }

        if (btnUnpair != null) {
            btnUnpair.setOnClickListener(v -> {
                Toast.makeText(this, "Unpairing...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                // TODO: Unpairing implementieren
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                SessionManager.logout(this);
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, StartController.class));
                finish();
            });
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT})
    private void loadFragment(Fragment fragment) {
        if (fragment == null) return;

        if (currentFragment != null && fragment.getClass().equals(currentFragment.getClass())) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return; // vermeidet doppeltes Neuladen
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commitAllowingStateLoss(); // vermeidet Abstürze bei schnellen Wechseln

        currentFragment = fragment;
        drawerLayout.closeDrawer(GravityCompat.START);

        // Setze Fragment-Titel (optional)
        if (fragment instanceof BaseFragment && headerName != null) {
            String title = ((BaseFragment) fragment).getTitle();
            headerName.setText(title != null ? title : "Wechselgeld");
        }

        // Starte Bluetooth-Server nur bei Transaktion
        if (fragment instanceof TransactionFragment) {
            try {
                BluetoothManager.getInstance(this, null).startServer();
            } catch (Exception e) {
                Log.e(TAG, "Bluetooth server start failed: " + e.getMessage());
            }
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
