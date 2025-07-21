package htl.steyr.wechselgeldapp.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import htl.steyr.wechselgeldapp.UI.Fragments.Seller.ConnectFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Seller.HistoryFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Seller.HomeFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Seller.ProfileFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Seller.TransactionFragment;
import htl.steyr.wechselgeldapp.Utilities.Security.SessionManager;

/**
 * This class controls the seller-side user interface in the Wechselgeld app.
 * It manages navigation, fragment loading, drawer handling, and user session events like logout.
 */
public class SellerUIController extends AppCompatActivity {

    private TextView headerName;
    private DrawerLayout drawerLayout;

    /**
     * Called when the activity is created. Initializes the UI, navigation drawer, and loads the default fragment.
     *
     * @param savedInstanceState The previously saved state (if any).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_ui);

        // Initialize views
        headerName = findViewById(R.id.restaurant_name);
        drawerLayout = findViewById(R.id.drawer_layout);

        // Load saved display name from preferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String displayName = prefs.getString("user_display_name", "Verkäufer");
        if (headerName != null) {
            headerName.setText(displayName);
        }

        // Setup drawer open/close buttons
        ImageButton menuButton = findViewById(R.id.menu_icon);
        ImageButton closeButton = findViewById(R.id.btn_close);

        if (menuButton != null) {
            menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        }

        // Setup navigation icons
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

        // Setup menu buttons
        MaterialButton btnBackup = findViewById(R.id.btn_backup);
        MaterialButton btnUnpair = findViewById(R.id.btn_unpair);
        MaterialButton btnLogout = findViewById(R.id.btn_logout);

        if (btnBackup != null) {
            btnBackup.setOnClickListener(v -> {
                Toast.makeText(this, "Creating backup...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                // TODO: Trigger backup logic
            });
        }

        if (btnUnpair != null) {
            btnUnpair.setOnClickListener(v -> {
                Toast.makeText(this, "Unpairing device...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                // TODO: Trigger unpairing logic
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                SessionManager.logout(this);
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, StartController.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Load default fragment on start
        loadFragment(new HomeFragment());
    }

    /**
     * Replaces the current fragment in the container with the given fragment.
     * Also updates the header title if the fragment provides one.
     *
     * @param fragment The fragment to load.
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        if (fragment instanceof BaseFragment && headerName != null) {
            headerName.setText(((BaseFragment) fragment).getTitle());
        }
    }

    /**
     * Handles the back button press.
     * Closes the drawer if it's open; otherwise, performs default back navigation.
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