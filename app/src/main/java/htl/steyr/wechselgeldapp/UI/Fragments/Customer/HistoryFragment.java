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

/**
 * Fragment displaying a list of restaurants (sellers) the current customer is connected to.
 * The customer is identified based on the device UUID stored in the local database.
 */
public class HistoryFragment extends BaseFragment {

    private RecyclerView restaurantList;
    private DatabaseHelper dbHelper;
    private int currentCustomerId;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           LayoutInflater object that can be used to inflate views.
     * @param container          If non-null, this is the parent view.
     * @param savedInstanceState Saved state from a previous instance.
     * @return The root view of the fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_history, container, false);

        // Initialize database access
        dbHelper = new DatabaseHelper(getContext());
        currentCustomerId = getCurrentCustomerId();

        // Set up RecyclerView
        restaurantList = view.findViewById(R.id.restaurant_list);
        restaurantList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load restaurant data from database
        loadConnectedRestaurants();

        return view;
    }

    /**
     * Retrieves the customer ID associated with this device's UUID from the local database.
     *
     * @return Customer ID or -1 if not found.
     */
    private int getCurrentCustomerId() {
        String deviceUuid = getDeviceUuid();

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

    /**
     * Retrieves the Android device's unique ID.
     *
     * @return Device UUID string.
     */
    private String getDeviceUuid() {
        return Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    /**
     * Loads the list of connected restaurants from the database and sets up the RecyclerView.
     */
    private void loadConnectedRestaurants() {
        List<Seller> restaurants = getConnectedRestaurantsFromDatabase();
        RestaurantAdapter adapter = new RestaurantAdapter(restaurants);
        restaurantList.setAdapter(adapter);
    }

    /**
     * Queries the local SQLite database for sellers connected to the current customer.
     *
     * @return A list of Seller objects.
     */
    private List<Seller> getConnectedRestaurantsFromDatabase() {
        List<Seller> restaurants = new ArrayList<>();

        // Query that fetches distinct sellers connected to the current customer
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
                        "" // phone or other field left empty
                );
                restaurants.add(seller);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return restaurants;
    }

    /**
     * Closes the database connection when the fragment is destroyed.
     */
    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    /**
     * Returns the title of the fragment, shown in the toolbar or tab.
     *
     * @return Title string.
     */
    @Override
    public String getTitle() {
        return "Meine Restaurants";
    }

    /**
     * RecyclerView Adapter to display a list of Seller objects.
     */
    private static class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
        private final List<Seller> restaurants;

        /**
         * Constructs the adapter with a list of sellers.
         *
         * @param restaurants List of Seller objects.
         */
        public RestaurantAdapter(List<Seller> restaurants) {
            this.restaurants = restaurants;
        }

        /**
         * Creates a new ViewHolder for a restaurant card.
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Create a CardView dynamically to hold restaurant details
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

            // Create a vertical layout inside the CardView
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            // Create and configure a TextView for the restaurant name
            TextView nameView = new TextView(parent.getContext());
            nameView.setId(View.generateViewId());
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            nameView.setTypeface(null, Typeface.BOLD);

            // Create and configure a TextView for the restaurant email
            TextView emailView = new TextView(parent.getContext());
            emailView.setId(View.generateViewId());
            emailView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            emailView.setTextColor(Color.GRAY);

            // Add views to layout and layout to card
            layout.addView(nameView);
            layout.addView(emailView);
            cardView.addView(layout);

            return new ViewHolder(cardView, nameView, emailView);
        }

        /**
         * Binds the data from a Seller object to the views inside the ViewHolder.
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Seller restaurant = restaurants.get(position);
            holder.nameView.setText(restaurant.getShopName());
            holder.emailView.setText(restaurant.getEmail());
        }

        /**
         * Returns the number of items to display in the RecyclerView.
         */
        @Override
        public int getItemCount() {
            return restaurants.size();
        }

        /**
         * ViewHolder that holds references to the TextViews for each seller.
         */
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameView;
            TextView emailView;

            /**
             * Constructor for the ViewHolder.
             *
             * @param itemView  The root view (CardView).
             * @param nameView  TextView for the restaurant name.
             * @param emailView TextView for the restaurant email.
             */
            ViewHolder(View itemView, TextView nameView, TextView emailView) {
                super(itemView);
                this.nameView = nameView;
                this.emailView = emailView;
            }
        }
    }
}
