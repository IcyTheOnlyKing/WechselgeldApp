package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

/**
 * HistoryFragment displays the transaction history for the seller.
 * Currently, it only inflates a static layout without dynamic data.
 */
public class HistoryFragment extends BaseFragment {

    /**
     * Called to create the view for this fragment.
     * Inflates the layout defined in seller_fragment_history.xml.
     *
     * @param inflater used to inflate the layout
     * @param container the parent view this fragment will be attached to
     * @param savedInstanceState saved state (if any)
     * @return the inflated view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.seller_fragment_history, container, false);
    }

    /**
     * Returns the title of the fragment used for UI headers.
     *
     * @return the title as a string
     */
    @Override
    public String getTitle() {
        return "Verlauf";
    }
}
