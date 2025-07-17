package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.Database.Models.Seller;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class HistoryFragment extends BaseFragment {

    private RecyclerView restaurantList;
    private DatabaseHelper dbHelper;
    private int currentCustomerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_history, container, false);

        // Initialize database helper
        dbHelper = new DatabaseHelper(getContext());
        currentCustomerId = getCurrentCustomerId();

        // Initialize RecyclerView
        restaurantList = view.findViewById(R.id.restaurant_list);
        restaurantList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load data
        loadConnectedRestaurants();

        return view;
    }

    private int getCurrentCustomerId() {
        String deviceUuid = getDeviceUuid();

        // Then query the database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT customerId FROM Device WHERE uuid = ?",
                new String[]{deviceUuid});

        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    // Helper method to get device UUID
    private String getDeviceUuid() {
        return Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    private void loadConnectedRestaurants() {
        List<Seller> restaurants = getConnectedRestaurantsFromDatabase();
        RestaurantAdapter adapter = new RestaurantAdapter(restaurants);
        restaurantList.setAdapter(adapter);
    }

    private List<Seller> getConnectedRestaurantsFromDatabase() {
        List<Seller> restaurants = new ArrayList<>();

        // Beispiel-Query - muss noch angepasst werden
        String query = "SELECT DISTINCT s.id, s.shopName, s.email " +
                "FROM Seller s " +
                "JOIN CustomerSeller cs ON s.id = cs.seller_id " +
                "WHERE cs.customer_id = ?";

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query,
                new String[]{String.valueOf(currentCustomerId)});

        if (cursor.moveToFirst()) {
            do {
                Seller seller = new Seller(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("shopName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        ""
                );
                restaurants.add(seller);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return restaurants;
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public String getTitle() {
        return "Meine Restaurants";
    }

    private static class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
        private final List<Seller> restaurants;

        public RestaurantAdapter(List<Seller> restaurants) {
            this.restaurants = restaurants;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView cardView = new CardView(parent.getContext());
            cardView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            cardView.setCardElevation(4f);
            cardView.setRadius(8f);
            cardView.setContentPadding(16, 16, 16, 16);
            cardView.setCardBackgroundColor(Color.WHITE);
            cardView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));

            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView nameView = new TextView(parent.getContext());
            nameView.setId(View.generateViewId());
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            nameView.setTypeface(null, Typeface.BOLD);

            TextView emailView = new TextView(parent.getContext());
            emailView.setId(View.generateViewId());
            emailView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            emailView.setTextColor(Color.GRAY);

            layout.addView(nameView);
            layout.addView(emailView);
            cardView.addView(layout);

            return new ViewHolder(cardView, nameView, emailView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Seller restaurant = restaurants.get(position);
            holder.nameView.setText(restaurant.getShopName());
            holder.emailView.setText(restaurant.getEmail());
        }

        @Override
        public int getItemCount() {
            return restaurants.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameView;
            TextView emailView;

            ViewHolder(View itemView, TextView nameView, TextView emailView) {
                super(itemView);
                this.nameView = nameView;
                this.emailView = emailView;
            }
        }
    }
}