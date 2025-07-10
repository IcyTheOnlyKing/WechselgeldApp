package htl.steyr.wechselgeldapp.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.ConnectFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.HistoryFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.HomeFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Customer.TransactionFragment;

public class CustomerUIController extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private DrawerLayout drawerLayout;

    private String currentOtherUuid;

    // Header views
    private LinearLayout headerLayout;
    private ImageView menuIcon;
    private EditText restaurantNameEditText;
    private ImageView profileImage;

    // Bottom bar icons
    private LinearLayout homeLayout;
    private LinearLayout connectLayout;
    private LinearLayout transactionLayout;
    private LinearLayout historyLayout;

    // Sidebar buttons
    private ImageButton btnClose;
    private com.google.android.material.button.MaterialButton btnBackup;
    private com.google.android.material.button.MaterialButton btnUnpair;
    private com.google.android.material.button.MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_ui);

        currentOtherUuid = getIntent().getStringExtra("UUID");
        if (currentOtherUuid == null) {
            currentOtherUuid = "demo-uuid"; // Temporärer Fallback
        }

        // DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Header Bar Views (top_app_bar.xml)
        headerLayout = findViewById(R.id.header);
        menuIcon = findViewById(R.id.menu_icon);
        restaurantNameEditText = findViewById(R.id.restaurant_name);
        profileImage = findViewById(R.id.profile_image);

        // Bottom bar icons
        homeLayout = findViewById(R.id.homeIcon);
        connectLayout = findViewById(R.id.connectIcon);
        transactionLayout = findViewById(R.id.transactionIcon);
        historyLayout = findViewById(R.id.historyIcon);

        // Sidebar buttons (sidebar.xml)
        btnClose = findViewById(R.id.btn_close);
        btnBackup = findViewById(R.id.btn_backup);
        btnUnpair = findViewById(R.id.btn_unpair);
        btnLogout = findViewById(R.id.btn_logout);

        // DB Initialisierung
        dbHelper = new DatabaseHelper(this);

        // Menü-Icon öffnet das Drawer
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Close-Button schließt das Drawer
        btnClose.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        // Sidebar Button Click Listener
        btnBackup.setOnClickListener(v -> {
            Toast.makeText(this, "Backup gestartet...", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
            // TODO: Backup-Logik implementieren
        });

        btnUnpair.setOnClickListener(v -> {
            Toast.makeText(this, "Kopplung wird getrennt...", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
            // TODO: Unpair-Logik implementieren
        });

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Abmelden...", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
            // TODO: Logout-Logik implementieren (z.B. Session löschen, LoginActivity starten)
        });

        // Shopname laden
        loadDisplayName();

        // Default Fragment mit UUID übergeben
        loadFragment(createHomeFragment());

        // Bottom bar Navigation
        homeLayout.setOnClickListener(v -> loadFragment(createHomeFragment()));
        connectLayout.setOnClickListener(v -> loadFragment(new ConnectFragment()));
        transactionLayout.setOnClickListener(v -> loadFragment(new TransactionFragment()));
        historyLayout.setOnClickListener(v -> loadFragment(new HistoryFragment()));
    }

    private HomeFragment createHomeFragment() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("UUID", currentOtherUuid);
        fragment.setArguments(args);
        return fragment;
    }

    private void loadDisplayName() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String displayName = prefs.getString("user_display_name", null);

        if (displayName != null && !displayName.isEmpty()) {
            restaurantNameEditText.setText("Willkommen " + displayName + "!");
        } else {
            restaurantNameEditText.setText("Willkommen!");
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Falls Drawer offen, dann schließen, sonst normal zurück
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
