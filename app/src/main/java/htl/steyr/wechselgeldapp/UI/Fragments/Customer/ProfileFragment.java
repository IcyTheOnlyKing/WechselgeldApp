package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;
import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.Utilities.Security.SessionManager;

/**
 * ProfileFragment shows the profile of the logged-in customer.
 * It allows the user to view and edit their name and email address.
 */
public class ProfileFragment extends BaseFragment {

    private DatabaseHelper db;
    private TextInputEditText etName, etEmail;

    /**
     * Inflates the layout and initializes the database helper.
     *
     * @param inflater Used to inflate the layout
     * @param container The parent view that the fragment UI will be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * @return The root view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = new DatabaseHelper(requireContext());
        return inflater.inflate(R.layout.customer_fragment_profile, container, false);
    }

    /**
     * Called after the view is created. Sets up UI and loads customer data from database.
     *
     * @param view The root view returned by onCreateView
     * @param savedInstanceState If non-null, the fragment is being re-created
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        MaterialButton saveButton = view.findViewById(R.id.btn_save);

        // Get customer ID from session
        int customerId = SessionManager.getCurrentUserId(requireContext());

        // If a valid ID was found, load profile and allow editing
        if (customerId != -1) {
            loadCustomerProfile(customerId);

            saveButton.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                db.updateCustomerProfile(customerId, name, email);
            });
        }
    }

    /**
     * Loads the profile data from the database and fills the UI fields.
     *
     * @param customerId ID of the current customer
     */
    private void loadCustomerProfile(int customerId) {
        Cursor cursor = db.getCustomerProfile(customerId);
        if (cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("displayName")));
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        }
        cursor.close();
    }

    /**
     * Closes the database when the fragment is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) {
            db.close();
        }
    }

    /**
     * Returns the fragment title that is shown in the UI.
     *
     * @return A string representing the title
     */
    @Override
    public String getTitle() {
        return "Profil";
    }
}
