package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;
import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.Utilities.Security.SessionManager;
import htl.steyr.wechselgeldapp.Utilities.Security.SecureData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Toast;

public class ProfileFragment extends BaseFragment {

    private DatabaseHelper db;
    private TextInputEditText etName, etEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = new DatabaseHelper(requireContext());
        return inflater.inflate(R.layout.customer_fragment_profile, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        MaterialButton saveButton = view.findViewById(R.id.btn_save);
        MaterialButton changePassword = view.findViewById(R.id.btn_change_password);

        int customerId = SessionManager.getCurrentUserId(requireContext());
        if (customerId != -1) {
            loadCustomerProfile(customerId);

            saveButton.setOnClickListener(v -> {
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();
                db.updateCustomerProfile(customerId, name, email);
            });

            changePassword.setOnClickListener(v -> showChangePasswordDialog(customerId));
        }
    }

    private void loadCustomerProfile(int customerId) {
        Cursor cursor = db.getCustomerProfile(customerId);
        if (cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("displayName")));
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        }
        cursor.close();
    }

    private void showChangePasswordDialog(int customerId) {
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

                    String storedHash = db.getCustomerPasswordHashById(customerId);
                    if (storedHash == null || !SecureData.checkPassword(oldPass, storedHash)) {
                        Toast.makeText(requireContext(), "Altes Passwort falsch", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String newHash = SecureData.hashPasswordViaBCrypt(newPass);
                    db.updateCustomerPassword(customerId, newHash);
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

    @Override
    public String getTitle() {
        return "Profil";
    }
}