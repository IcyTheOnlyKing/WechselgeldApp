package htl.steyr.wechselgeldapp.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

public class SellerUIController extends AppCompatActivity {

    private TextView headerName;
    private DrawerLayout drawerLayout;
    private Fragment currentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_ui);

        headerName = findViewById(R.id.restaurant_name);
        drawerLayout = findViewById(R.id.drawer_layout);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String displayName = prefs.getString("user_display_name", "Verkäufer");
        if (headerName != null) {
            headerName.setText(displayName);
        }

        setupDrawerButtons();
        setupNavigation();

        // Lade Startfragment (Home)
        loadFragment(new HomeFragment());
    }

    private void setupDrawerButtons() {
        ImageButton menuButton = findViewById(R.id.menu_icon);
        ImageButton closeButton = findViewById(R.id.btn_close);

        if (menuButton != null) {
            menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        }

        MaterialButton btnBackup = findViewById(R.id.btn_backup);
        MaterialButton btnUnpair = findViewById(R.id.btn_unpair);
        MaterialButton btnLogout = findViewById(R.id.btn_logout);

        if (btnBackup != null) {
            btnBackup.setOnClickListener(v -> {
                Toast.makeText(this, "Backup wird erstellt...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                // TODO: Backup-Logik integrieren
            });
        }

        if (btnUnpair != null) {
            btnUnpair.setOnClickListener(v -> {
                Toast.makeText(this, "Kopplung wird aufgehoben...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                // TODO: Unpair-Logik integrieren
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Toast.makeText(this, "Abmeldung...", Toast.LENGTH_SHORT).show();
                SessionManager.logout(this);
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, StartController.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupNavigation() {
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
    }

    private void loadFragment(Fragment fragment) {
        if (fragment == null) return;

        // Verhindere doppeltes Neuladen des selben Fragments
        if (currentFragment != null && fragment.getClass().equals(currentFragment.getClass())) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commitAllowingStateLoss();

        currentFragment = fragment;
        drawerLayout.closeDrawer(GravityCompat.START);

        // Setze Überschrift passend zum Fragment
        if (fragment instanceof BaseFragment && headerName != null) {
            String title = ((BaseFragment) fragment).getTitle();
            headerName.setText(title != null ? title : "Wechselgeld");
        }

        Log.d("SellerUI", "Fragment geladen: " + fragment.getClass().getSimpleName());
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
