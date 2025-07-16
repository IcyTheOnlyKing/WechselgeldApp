package htl.steyr.wechselgeldapp.Utilities;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import htl.steyr.wechselgeldapp.R;

public class BottomNavigationHelper {
    public static void setupBottomNavigation(LinearLayout bottomNavigation, int activeTab) {
        // Reset all tabs to inactive state
        resetTabs(bottomNavigation);

        // Set the active tab
        switch (activeTab) {
            case 0: // Home
                setTabActive(bottomNavigation, R.id.home_icon, R.id.home_text);
                break;
            case 1: // Connect
                setTabActive(bottomNavigation, R.id.connect_icon, R.id.connect_text);
                break;
            case 2: // Transactions
                setTabActive(bottomNavigation, R.id.transactions_icon, R.id.transactions_text);
                break;
            case 3: // Wallet
                setTabActive(bottomNavigation, R.id.wallet_icon, R.id.wallet_text);
                break;
        }
    }

    private static void resetTabs(LinearLayout bottomNavigation) {
        int[] iconIds = {R.id.home_icon, R.id.connect_icon, R.id.transactions_icon, R.id.wallet_icon};
        int[] textIds = {R.id.home_text, R.id.connect_text, R.id.transactions_text, R.id.wallet_text};

        for (int i = 0; i < iconIds.length; i++) {
            ImageView icon = bottomNavigation.findViewById(iconIds[i]);
            TextView text = bottomNavigation.findViewById(textIds[i]);

            if (icon != null) {
                icon.setColorFilter(ContextCompat.getColor(bottomNavigation.getContext(), R.color.gray));
            }
            if (text != null) {
                text.setTextColor(ContextCompat.getColor(bottomNavigation.getContext(), R.color.gray));
            }
        }
    }

    private static void setTabActive(LinearLayout bottomNavigation, int iconId, int textId) {
        ImageView icon = bottomNavigation.findViewById(iconId);
        TextView text = bottomNavigation.findViewById(textId);

        if (icon != null) {
            icon.setColorFilter(ContextCompat.getColor(bottomNavigation.getContext(), R.color.black));
        }
        if (text != null) {
            text.setTextColor(ContextCompat.getColor(bottomNavigation.getContext(), R.color.black));
        }
    }
}
