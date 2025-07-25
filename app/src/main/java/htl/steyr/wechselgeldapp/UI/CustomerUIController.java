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

/**
 * CustomerUIController is the main activity for customer users.
 * It controls the navigation between fragments, the side drawer,
 * and Bluetooth server start on transaction.
 */
public class CustomerUIController extends AppCompatActivity {

    private static final String TAG = "CustomerUIController";

    /** Displays the customer name or profile label */
    private TextView headerName;

    /** The side drawer layout used for navigation and settings */
    private DrawerLayout drawerLayout;

    /** The currently active fragment */
    private Fragment currentFragment;

    /**
     * Called when the activity is first created.
     * Initializes UI components and loads the home fragment.
     *
     * @param savedInstanceState the previously saved state (if any)
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_ui);

        initializeViews();     // Setup header and drawer
        setupNavigation();     // Setup button click listeners
        loadFragment(new HomeFragment()); // Load initial fragment
    }

    /**
     * Finds and initializes header and drawer views,
     * and sets the display name of the customer.
     */
    private void initializeViews() {
        headerName = findViewById(R.id.restaurant_name);
        drawerLayout = findViewById(R.id.drawer_layout);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String displayName = prefs.getString("user_display_name", "Customer");

        if (headerName != null) {
            headerName.setText(displayName);
        }
    }

    /**
     * Sets up navigation listeners for drawer buttons and icons.
     * Requires Bluetooth permissions since ConnectFragment and TransactionFragment may trigger Bluetooth actions.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT})
    private void setupNavigation() {
        ImageButton menuButton = findViewById(R.id.menu_icon);
        ImageButton closeButton = findViewById(R.id.btn_close);

        // Open drawer menu
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            });
        }

        // Close drawer menu
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        // Set up navigation icons
        findViewById(R.id.homeIcon).setOnClickListener(v -> loadFragment(new HomeFragment()));
        findViewById(R.id.connectIcon).setOnClickListener(v -> loadFragment(new ConnectFragment()));
        findViewById(R.id.transactionIcon).setOnClickListener(v -> loadFragment(new TransactionFragment()));
        findViewById(R.id.historyIcon).setOnClickListener(v -> loadFragment(new HistoryFragment()));
        findViewById(R.id.profile_image).setOnClickListener(v -> loadFragment(new ProfileFragment()));

        // Handle drawer buttons
        MaterialButton btnBackup = findViewById(R.id.btn_backup);
        MaterialButton btnUnpair = findViewById(R.id.btn_unpair);
        MaterialButton btnLogout = findViewById(R.id.btn_logout);

        // Backup button (logic not yet implemented)
        if (btnBackup != null) {
            btnBackup.setOnClickListener(v -> {
                Toast.makeText(this, "Creating backup...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                // TODO: Implement backup logic
            });
        }

        // Unpair button (logic not yet implemented)
        if (btnUnpair != null) {
            btnUnpair.setOnClickListener(v -> {
                Toast.makeText(this, "Unpairing...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                // TODO: Implement unpairing logic
            });
        }

        // Logout button
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

    /**
     * Loads the given fragment into the main container.
     * Prevents reloading if the same fragment is already active.
     * Starts the Bluetooth server if TransactionFragment is loaded.
     *
     * @param fragment the fragment to be loaded
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT})
    private void loadFragment(Fragment fragment) {
        if (fragment == null) return;

        // Avoid reloading the same fragment
        if (currentFragment != null && fragment.getClass().equals(currentFragment.getClass())) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commitAllowingStateLoss();

        currentFragment = fragment;
        drawerLayout.closeDrawer(GravityCompat.START);

        // Update header title based on fragment
        if (fragment instanceof BaseFragment && headerName != null) {
            String title = ((BaseFragment) fragment).getTitle();
            headerName.setText(title != null ? title : "Wechselgeld");
        }

        // Automatically start Bluetooth server when TransactionFragment is active
        if (fragment instanceof TransactionFragment) {
            try {
                BluetoothManager.getInstance(this, null).startServer();
            } catch (Exception e) {
                Log.e(TAG, "Bluetooth server start failed: " + e.getMessage());
            }
        }
    }

    /**
     * Handles back button presses.
     * Closes the drawer if it's open, otherwise lets the system handle it.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
