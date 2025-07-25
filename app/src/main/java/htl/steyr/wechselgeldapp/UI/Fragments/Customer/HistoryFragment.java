package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

/**
 * HistoryFragment shows the transaction history for the customer.
 * Currently, it only loads a static layout without dynamic content.
 */
public class HistoryFragment extends BaseFragment {

    /**
     * Inflates the layout for the history view.
     *
     * @param inflater LayoutInflater to inflate the view
     * @param container Parent container of the fragment
     * @param savedInstanceState Saved instance state (if any)
     * @return The inflated view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.customer_fragment_history, container, false);
    }

    /**
     * Returns the title of the fragment, used in headers or navigation.
     *
     * @return The title string
     */
    @Override
    public String getTitle() {
        return "Verlauf";
    }
}
