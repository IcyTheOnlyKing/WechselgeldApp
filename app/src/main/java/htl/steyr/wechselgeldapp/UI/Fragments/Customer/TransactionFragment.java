package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.IOException;

import htl.steyr.wechselgeldapp.Bluetooth.BluetoothDataService;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

/**
 * Fragment for the customer side to display and handle incoming transaction data
 * received via Bluetooth from the seller device.
 */
public class TransactionFragment extends BaseFragment {

    private TextView tvInvoiceAmount, tvRemainingAmount;

    /**
     * Inflates the transaction layout and initializes Bluetooth data listening
     * for receiving invoice amounts from the seller.
     *
     * @param inflater The LayoutInflater object used to inflate views in the fragment.
     * @param container The parent view that the fragment UI should be attached to.
     * @param savedInstanceState Saved state information, if available.
     * @return The root view of the inflated layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_transaction, container, false);

        tvInvoiceAmount = view.findViewById(R.id.tvInvoiceAmount);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);

        // Check if there is an active Bluetooth connection
        if (BluetoothManager.getInstance().getConnectedSocket() != null) {
            // Handler to process messages from Bluetooth input stream
            Handler handler = new Handler(Looper.getMainLooper()) {
                @SuppressLint("SetTextI18n")
                @Override
                public void handleMessage(@NonNull Message msg) {
                    String message = (String) msg.obj;

                    // Expected format: "amount:12.34"
                    if (message.startsWith("amount:")) {
                        String amount = message.substring(7);
                        tvInvoiceAmount.setText("€" + amount);
                        tvRemainingAmount.setText("€" + amount); // Initially, full amount is remaining
                    }
                }
            };

            try {
                // Start listening for messages over Bluetooth
                BluetoothDataService service = new BluetoothDataService(
                        BluetoothManager.getInstance().getConnectedSocket(), handler);
                service.listenForMessages();
            } catch (IOException e) {
                e.printStackTrace();
                // Could add a Toast or callback for user feedback here
            }
        }

        return view;
    }

    /**
     * Returns the title string for this fragment used in the app header.
     *
     * @return "Transaktionen"
     */
    @Override
    public String getTitle() {
        return "Transaktionen";
    }
}
