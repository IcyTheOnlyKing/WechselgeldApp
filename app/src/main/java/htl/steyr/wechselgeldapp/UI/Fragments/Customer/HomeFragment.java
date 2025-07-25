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
 * HomeFragment is the start screen for the customer.
 * It currently only inflates a static layout without dynamic logic.
 * You can later add buttons or status information here.
 */
public class HomeFragment extends BaseFragment {

    /**
     * Creates and returns the UI view for this fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * @return The root view of the fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {return inflater.inflate(R.layout.customer_fragment_home, container, false);}

    /**
     * Called immediately after onCreateView.
     * You can use this method to initialize views or logic.
     *
     * @param view The root view returned by onCreateView
     * @param savedInstanceState If non-null, the fragment is being re-created
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {super.onViewCreated(view, savedInstanceState);}

    /**
     * Returns the title shown in the UI for this fragment.
     *
     * @return Title string for this fragment
     */
    @Override
    public String getTitle() {return "Wechselgeld App";}
}
