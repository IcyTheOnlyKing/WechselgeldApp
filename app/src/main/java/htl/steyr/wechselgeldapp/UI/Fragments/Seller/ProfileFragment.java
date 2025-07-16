package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class ProfileFragment extends BaseFragment {

    private DatabaseHelper db;
    private final Map<String, String> initialValues = new HashMap<>();

    private TextInputEditText name, email, street, houseNumber, zip, city;

    private int sellerId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String shopName = prefs.getString("user_display_name", null);

        if (shopName != null) {
            String id = db.getSellerIdByName(shopName);
            if (id != null) {
                sellerId = Integer.parseInt(id);
            }
        }

        if (sellerId != -1) {
            loadProfileData();
        }

        saveChanges.setOnClickListener(v -> {
            if (sellerId == -1) {
                Toast.makeText(getContext(), "Fehler beim Speichern", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor c = db.getPersonalInfoBySellerId(sellerId);
            if (c.moveToFirst()) {
                db.updatePersonalInfo(
                        sellerId,
                        name.getText().toString(),
                        email.getText().toString(),
                        street.getText().toString(),
                        houseNumber.getText().toString(),
                        zip.getText().toString(),
                        city.getText().toString()
                );
            } else {
                db.insertPersonalInfo(
                        sellerId,
                        name.getText().toString(),
                        email.getText().toString(),
                        street.getText().toString(),
                        houseNumber.getText().toString(),
                        zip.getText().toString(),
                        city.getText().toString()
                );
            }
            c.close();
            Toast.makeText(getContext(), "Gespeichert", Toast.LENGTH_SHORT).show();
            storeInitialValues();
        });

        // Zurück-Button abfangen
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Ungespeicherte Änderungen")
                            .setMessage("Du hast ungespeicherte Änderungen. Möchtest du diese wirklich verwerfen?")
                            .setPositiveButton("Verwerfen", (dialog, which) -> requireActivity().getSupportFragmentManager().popBackStack())
                            .setNegativeButton("Abbrechen", null)
                            .show();
                } else {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private void loadProfileData() {
        Cursor cursor = db.getPersonalInfoBySellerId(sellerId);
        if (cursor.moveToFirst()) {
            name.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            email.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            street.setText(cursor.getString(cursor.getColumnIndexOrThrow("street")));
            houseNumber.setText(cursor.getString(cursor.getColumnIndexOrThrow("houseNumber")));
            zip.setText(cursor.getString(cursor.getColumnIndexOrThrow("zipCode")));
            city.setText(cursor.getString(cursor.getColumnIndexOrThrow("city")));
        }
        cursor.close();
        storeInitialValues();
    }

    private void storeInitialValues() {
        initialValues.put("name", name.getText().toString());
        initialValues.put("email", email.getText().toString());
        initialValues.put("street", street.getText().toString());
        initialValues.put("houseNumber", houseNumber.getText().toString());
        initialValues.put("zip", zip.getText().toString());
        initialValues.put("city", city.getText().toString());
    }

    private boolean hasUnsavedChanges() {
        return !name.getText().toString().equals(initialValues.get("name")) ||
                !email.getText().toString().equals(initialValues.get("email")) ||
                !street.getText().toString().equals(initialValues.get("street")) ||
                !houseNumber.getText().toString().equals(initialValues.get("houseNumber")) ||
                !zip.getText().toString().equals(initialValues.get("zip")) ||
                !city.getText().toString().equals(initialValues.get("city"));
    }

    @Override
    public String getTitle() {
        return "Profil";
    }
}
