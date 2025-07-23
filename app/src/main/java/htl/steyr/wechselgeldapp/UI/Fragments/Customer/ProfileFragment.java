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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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

        int customerId = SessionManager.getCurrentUserId(requireContext());
        if (customerId != -1) {
            loadCustomerProfile(customerId);

            saveButton.setOnClickListener(v -> {
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();
                db.updateCustomerProfile(customerId, name, email);
            });
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