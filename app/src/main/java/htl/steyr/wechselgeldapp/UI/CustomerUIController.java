package htl.steyr.wechselgeldapp.UI;

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

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.ProfileFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.ConnectFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.HistoryFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.HomeFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.TransactionFragment;

public class CustomerUIController extends AppCompatActivity {
    private static final String TAG = "CustomerUIController";
    private TextView headerName;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.customer_ui);
            Log.d(TAG, "Layout set successfully");

            // Initialize views
            initializeViews();

            // Setup navigation
            setupNavigation();

            // Load default fragment
            loadFragment(new HomeFragment());

            Log.d(TAG, "CustomerUIController initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Fehler beim Laden der Ansicht", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        try {
            // Initialize top app bar
            View topAppBar = findViewById(R.id.topAppBar);
            if (topAppBar != null) {
                headerName = topAppBar.findViewById(R.id.restaurant_name);
                if (headerName != null) {
                    // Set user display name from shared preferences
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    String displayName = prefs.getString("user_display_name", "Kunde");
                    headerName.setText(displayName);
                    Log.d(TAG, "Header name set to: " + displayName);
                } else {
                    Log.e(TAG, "restaurant_name TextView not found");
                }
            } else {
                Log.e(TAG, "topAppBar not found");
            }

            // Initialize drawer layout
            drawerLayout = findViewById(R.id.drawer_layout);
            if (drawerLayout == null) {
                Log.e(TAG, "drawer_layout not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in initializeViews: " + e.getMessage(), e);
        }
    }

    private void setupNavigation() {
        try {
            // Menu button
            ImageButton menuButton = findViewById(R.id.menu_icon);
            if (menuButton != null) {
                menuButton.setOnClickListener(v -> {
                    if (drawerLayout != null) {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                });
            }

            // Close button in drawer
            ImageButton closeButton = findViewById(R.id.btn_close);
            if (closeButton != null) {
                closeButton.setOnClickListener(v -> {
                    if (drawerLayout != null) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                });
            }

            // Bottom navigation
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

            Log.d(TAG, "Navigation setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error in setupNavigation: " + e.getMessage(), e);
        }
    }

    private void loadFragment(Fragment fragment) {
        try {
            Log.d(TAG, "Loading fragment: " + fragment.getClass().getSimpleName());

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();

            if (fragment instanceof BaseFragment && headerName != null) {
                String title = ((BaseFragment) fragment).getTitle();
                headerName.setText(title);
                Log.d(TAG, "Fragment title set to: " + title);
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