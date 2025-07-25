package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.content.Context;
import android.database.Cursor;
import htl.steyr.wechselgeldapp.Utilities.Security.SessionManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.R;

/**
 * This fragment allows the seller to view and update their personal profile information.
 * The data is retrieved from and stored in a local SQLite database.
 */
public class ProfileFragment extends Fragment {

    /** Helper object to access the local database */
    private DatabaseHelper db;

    /** Input fields for seller profile information */
    private TextInputEditText name, email, street, houseNumber, zip, city;

    /**
     * Called to create the view hierarchy associated with the fragment.
     * Initializes the DatabaseHelper instance.
     *
     * @param inflater Used to inflate the layout.
     * @param container Parent view that the fragment UI should be attached to.
     * @param savedInstanceState Saved state of the fragment (if available).
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = new DatabaseHelper(requireContext());
        return inflater.inflate(R.layout.seller_fragment_profile, container, false);
    }

    /**
     * Called immediately after the view is created.
     * Binds the UI elements and loads the seller data if logged in.
     *
     * @param view The root view of the fragment.
     * @param savedInstanceState Saved state of the fragment (if available).
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize input fields
        name = view.findViewById(R.id.et_name);
        email = view.findViewById(R.id.et_email);
        street = view.findViewById(R.id.et_street);
        houseNumber = view.findViewById(R.id.et_house_number);
        zip = view.findViewById(R.id.et_zip);
        city = view.findViewById(R.id.et_city);
        MaterialButton saveChanges = view.findViewById(R.id.btn_save);

        // Get the currently logged-in seller's ID
        int sellerId = SessionManager.getCurrentUserId(requireContext());

        if (sellerId != -1) {
            // Load profile data from database
            loadSellerProfile(sellerId);

            // Handle save button click
            saveChanges.setOnClickListener(v -> {
                // Read input values
                String shop = name.getText().toString();
                String mail = email.getText().toString();
                String str = street.getText().toString();
                String hNr = houseNumber.getText().toString();
                String plz = zip.getText().toString();
                String stadt = city.getText().toString();

                // Save changes to the database
                db.updateSellerProfile(sellerId, shop, mail);
                db.updatePersonalInfo(sellerId, shop, mail, str, hNr, plz, stadt);
            });
        }
    }

    /**
     * Loads seller profile data from the database and fills the input fields.
     *
     * @param sellerId The ID of the currently logged-in seller.
     */
    private void loadSellerProfile(int sellerId) {
        Cursor cursor = db.getSellerProfile(sellerId);
        if (cursor.moveToFirst()) {
            // Fill input fields with data from database
            name.setText(cursor.getString(cursor.getColumnIndexOrThrow("shopName")));
            email.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            street.setText(cursor.getString(cursor.getColumnIndexOrThrow("street")));
            houseNumber.setText(cursor.getString(cursor.getColumnIndexOrThrow("houseNumber")));
            zip.setText(cursor.getString(cursor.getColumnIndexOrThrow("zipCode")));
            city.setText(cursor.getString(cursor.getColumnIndexOrThrow("city")));
        }
        cursor.close();
    }

    /**
     * Called when the view is destroyed.
     * Closes the database connection to free resources.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) {
            db.close();
        }
    }
}
