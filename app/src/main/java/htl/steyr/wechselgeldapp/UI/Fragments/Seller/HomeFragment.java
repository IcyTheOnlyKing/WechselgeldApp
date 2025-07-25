package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import htl.steyr.wechselgeldapp.Bluetooth.BluetoothDeviceAdapter;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

/**
 * HomeFragment is the start screen for the seller section.
 * It may later be used to display recently connected Bluetooth devices or general info.
 */
public class HomeFragment extends BaseFragment {

    /** RecyclerView for showing paired or known Bluetooth devices (not yet implemented) */
    private RecyclerView deviceListRecyclerView;

    /** Adapter for displaying Bluetooth devices */
    private BluetoothDeviceAdapter bluetoothDeviceAdapter;

    /** List of Bluetooth devices shown in the RecyclerView */
    private List<BluetoothDevice> deviceList;

    /**
     * Called to create the view hierarchy for this fragment.
     * Inflates the layout defined in seller_fragment_home.xml.
     *
     * @param inflater Used to inflate the layout
     * @param container The parent view this fragment is attached to
     * @param savedInstanceState Saved state (if any)
     * @return The root view of the fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.seller_fragment_home, container, false);
    }

    /**
     * Called after the view has been created.
     * Useful for initializing views and logic (currently empty).
     *
     * @param view The root view of the fragment
     * @param savedInstanceState Saved state (if any)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // You can initialize views and logic here later
    }

    /**
     * Returns the title shown in the header or toolbar for this fragment.
     *
     * @return A string title
     */
    @Override
    public String getTitle() {
        return "Wechselgeld App";
    }
}
