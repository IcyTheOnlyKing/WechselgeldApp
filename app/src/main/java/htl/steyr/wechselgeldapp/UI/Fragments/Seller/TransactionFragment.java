package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

/**
 * Fragment for the seller to enter and send a payment amount to the connected customer via Bluetooth.
 */
public class TransactionFragment extends BaseFragment {

    private EditText etPaymentAmount;
    private MaterialButton btnSendPayment;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_transactions, container, false);

        etPaymentAmount = view.findViewById(R.id.etPaymentAmount);
        btnSendPayment = view.findViewById(R.id.btnSendPayment);

        btnSendPayment.setOnClickListener(v -> {
            String amount = etPaymentAmount.getText().toString().trim();
            if (amount.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            Bluetooth bluetooth = BluetoothManager.getInstance();
            if (bluetooth != null && bluetooth.isConnected()) {
                bluetooth.sendRawMessage("amount:" + amount);
                Toast.makeText(getContext(), "Amount sent: €" + amount, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No active Bluetooth connection", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /**
     * Returns the title for this fragment, used in the header of the UI.
     *
     * @return The title string.
     */
    @Override
    public String getTitle() {
        return "Transactions";
    }
}
