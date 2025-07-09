package htl.steyr.wechselgeldapp.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.HomeFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.SearchFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.SettingsFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.TransactionFragment;

public class CustomerUIController extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private MaterialToolbar topAppBar;
    private String currentOtherUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_ui);

        currentOtherUuid = getIntent().getStringExtra("UUID");
        if (currentOtherUuid == null) {
            currentOtherUuid = "demo-uuid"; // Temporärer Fallback
        }

        // View-Verknüpfungen
        topAppBar = findViewById(R.id.topAppBar);
        LinearLayout homeLayout = findViewById(R.id.homeLayout);
        LinearLayout searchLayout = findViewById(R.id.searchBTN);
        LinearLayout transactionLayout = findViewById(R.id.transactionLayout);
        LinearLayout settingsLayout = findViewById(R.id.settingsLayout);

        // DB-Initialisierung
        dbHelper = new DatabaseHelper(this);

        // Shopname laden
        loadDisplayName();

        // Default Fragment mit UUID übergeben
        loadFragment(createHomeFragment());

        homeLayout.setOnClickListener(v -> loadFragment(createHomeFragment()));
        searchLayout.setOnClickListener(v -> loadFragment(new SearchFragment()));
        transactionLayout.setOnClickListener(v -> loadFragment(new TransactionFragment()));
        settingsLayout.setOnClickListener(v -> loadFragment(new SettingsFragment()));
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
            topAppBar.setTitle("Willkommen " + displayName + "!");
        } else {
            topAppBar.setTitle("Willkommen!");
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
}