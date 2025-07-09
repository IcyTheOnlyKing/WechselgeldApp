package htl.steyr.wechselgeldapp.UI.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import htl.steyr.wechselgeldapp.AuthController;
import htl.steyr.wechselgeldapp.R;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the fragment_settings layout (now with a logout button)
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Find the logout button from the layout
        Button logoutButton = view.findViewById(R.id.logoutButton);

        // Set up click listener to trigger logout
        logoutButton.setOnClickListener(v -> logout());

        return view;
    }

    /**
     * Logs the user out by clearing login info and returning to AuthController (login screen).
     */
    private void logout() {
        // Get stored login data from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Set "is_logged_in" to false (logs out the user)
        prefs.edit().putBoolean("is_logged_in", false).apply();

        // Create intent to go back to login screen
        Intent intent = new Intent(requireContext(), AuthController.class);

        // Clear backstack so user can't press back to return
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Start login screen
        startActivity(intent);
    }
}
