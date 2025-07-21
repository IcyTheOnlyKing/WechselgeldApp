package htl.steyr.wechselgeldapp.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Seller.ConnectFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Seller.HistoryFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Seller.HomeFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Seller.ProfileFragment;
import htl.steyr.wechselgeldapp.UI.Fragments.Seller.TransactionFragment;

public class SellerUIController extends AppCompatActivity {

    private TextView headerName; // Geändert von EditText zu TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_ui);

        headerName = findViewById(R.id.restaurant_name);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageButton menuButton = findViewById(R.id.menu_icon);
        ImageButton closeButton = findViewById(R.id.btn_close);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        LinearLayout homeIcon = findViewById(R.id.homeIcon);
        LinearLayout connectIcon = findViewById(R.id.connectIcon);
        LinearLayout transactionIcon = findViewById(R.id.transactionIcon);
        LinearLayout historyIcon = findViewById(R.id.historyIcon);
        ImageView profileIcon = findViewById(R.id.profile_image);

        homeIcon.setOnClickListener(v -> loadFragment(new HomeFragment()));
        connectIcon.setOnClickListener(v -> loadFragment(new ConnectFragment()));
        transactionIcon.setOnClickListener(v -> loadFragment(new TransactionFragment()));
        historyIcon.setOnClickListener(v -> loadFragment(new HistoryFragment()));
        profileIcon.setOnClickListener(v -> loadFragment(new ProfileFragment()));

        loadFragment(new HomeFragment());
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        if (fragment instanceof BaseFragment) {
            headerName.setText(((BaseFragment) fragment).getTitle());
        }
    }
}