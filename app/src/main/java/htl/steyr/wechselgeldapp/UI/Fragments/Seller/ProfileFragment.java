package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.content.Context;
import android.content.SharedPreferences;
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
 * ProfileFragment allows the seller to view and update their personal information.
 * Data is loaded from and saved to the local SQLite database.
 */
public class ProfileFragment extends Fragment {

    private DatabaseHelper db;

    private TextInputEditText name, email, street, houseNumber, zip, city;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = new DatabaseHelper(requireContext());
        return inflater.inflate(R.layout.seller_fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name = view.findViewById(R.id.et_name);
        email = view.findViewById(R.id.et_email);
        street = view.findViewById(R.id.et_street);
        houseNumber = view.findViewById(R.id.et_house_number);
        zip = view.findViewById(R.id.et_zip);
        city = view.findViewById(R.id.et_city);
        MaterialButton saveChanges = view.findViewById(R.id.btn_save);

        // Load seller name from shared preferences
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String shopName = prefs.getString("user_display_name",null);

        name.setText(shopName);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) {
            db.close();
        }
    }
}
