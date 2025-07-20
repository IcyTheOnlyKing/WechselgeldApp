// app/src/main/java/htl/steyr/wechselgeldapp/Utilities/BottomNavigationHelper.java
            package htl.steyr.wechselgeldapp.Utilities;

            import android.widget.ImageView;
            import android.widget.LinearLayout;
            import android.widget.TextView;

            import androidx.core.content.ContextCompat;

            import htl.steyr.wechselgeldapp.R;

            public class BottomNavigationHelper {
                public static void setupBottomNavigation(LinearLayout bottomNavigation, int activeTab) {
                    resetTabs(bottomNavigation);

                    switch (activeTab) {
                        case 0: // Home
                            setTabActive(bottomNavigation, R.id.homeIcon);
                            break;
                        case 1: // Connect
                            setTabActive(bottomNavigation, R.id.connectIcon);
                            break;
                        case 2: // Transactions
                            setTabActive(bottomNavigation, R.id.transactionIcon);
                            break;
                        case 3: // Wallet/History
                            setTabActive(bottomNavigation, R.id.historyIcon);
                            break;
                    }
                }

                private static void resetTabs(LinearLayout bottomNavigation) {
                    int[] tabIds = {R.id.homeIcon, R.id.connectIcon, R.id.transactionIcon, R.id.historyIcon};

                    for (int tabId : tabIds) {
                        LinearLayout tab = bottomNavigation.findViewById(tabId);
                        if (tab != null) {
                            ImageView icon = (ImageView) tab.getChildAt(0);
                            TextView text = (TextView) tab.getChildAt(1);

                            if (icon != null) {
                                icon.setColorFilter(ContextCompat.getColor(bottomNavigation.getContext(), R.color.gray));
                            }
                            if (text != null) {
                                text.setTextColor(ContextCompat.getColor(bottomNavigation.getContext(), R.color.gray));
                            }
                        }
                    }
                }

                private static void setTabActive(LinearLayout bottomNavigation, int tabId) {
                    LinearLayout tab = bottomNavigation.findViewById(tabId);
                    if (tab != null) {
                        ImageView icon = (ImageView) tab.getChildAt(0);
                        TextView text = (TextView) tab.getChildAt(1);

                        if (icon != null) {
                            icon.setColorFilter(ContextCompat.getColor(bottomNavigation.getContext(), R.color.black));
                        }
                        if (text != null) {
                            text.setTextColor(ContextCompat.getColor(bottomNavigation.getContext(), R.color.black));
                        }
                    }
                }
            }