package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.database.Cursor;
import htl.steyr.wechselgeldapp.Utilities.Security.SessionManager;
import htl.steyr.wechselgeldapp.Utilities.Security.SecureData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.Toast;
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
        MaterialButton changePassword = view.findViewById(R.id.btn_change_password);

        int sellerId = SessionManager.getCurrentUserId(requireContext());
        if (sellerId != -1) {
            loadSellerProfile(sellerId);

            saveChanges.setOnClickListener(v -> {
                String shop = name.getText().toString();
                String mail = email.getText().toString();
                String str = street.getText().toString();
                String hNr = houseNumber.getText().toString();
                String plz = zip.getText().toString();
                String stadt = city.getText().toString();

                db.updateSellerProfile(sellerId, shop, mail);
                db.updatePersonalInfo(sellerId, shop, mail, str, hNr, plz, stadt);
            });

            changePassword.setOnClickListener(v -> showChangePasswordDialog(sellerId));
        }
    }

    private void loadSellerProfile(int sellerId) {
        Cursor cursor = db.getSellerProfile(sellerId);
        if (cursor.moveToFirst()) {
            name.setText(cursor.getString(cursor.getColumnIndexOrThrow("shopName")));
            email.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            street.setText(cursor.getString(cursor.getColumnIndexOrThrow("street")));
            houseNumber.setText(cursor.getString(cursor.getColumnIndexOrThrow("houseNumber")));
            zip.setText(cursor.getString(cursor.getColumnIndexOrThrow("zipCode")));
            city.setText(cursor.getString(cursor.getColumnIndexOrThrow("city")));
        }
        cursor.close();
    }

    private void showChangePasswordDialog(int sellerId) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        TextInputEditText oldPw = dialogView.findViewById(R.id.et_old_password);
        TextInputEditText newPw = dialogView.findViewById(R.id.et_new_password);
        TextInputEditText confirmPw = dialogView.findViewById(R.id.et_confirm_password);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Passwort ändern")
                .setView(dialogView)
                .setPositiveButton("Speichern", (d, w) -> {
                    String oldPass = oldPw.getText().toString();
                    String newPass = newPw.getText().toString();
                    String confirmPass = confirmPw.getText().toString();

                    if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                        Toast.makeText(requireContext(), "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(requireContext(), "Neue Passwörter stimmen nicht überein", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String storedHash = db.getSellerPasswordHashById(sellerId);
                    if (storedHash == null || !SecureData.checkPassword(oldPass, storedHash)) {
                        Toast.makeText(requireContext(), "Altes Passwort falsch", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String newHash = SecureData.hashPasswordViaBCrypt(newPass);
                    db.updateSellerPassword(sellerId, newHash);
                    Toast.makeText(requireContext(), "Passwort geändert", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) {
            db.close();
        }
    }
}
