package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import htl.steyr.wechselgeldapp.Backup.UserData;
import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

/**
 * TransactionFragment allows the seller to input a payment amount
 * and send it to the connected customer via Bluetooth.
 */
public class TransactionFragment extends BaseFragment {

    /** Input field for the payment amount */
    private EditText etPaymentAmount;

    /** Button to trigger sending the payment */
    private MaterialButton btnSendPayment;

    /**
     * Creates the view hierarchy and sets up Bluetooth callbacks and button listeners.
     *
     * @param inflater Used to inflate the layout
     * @param container Parent view that the fragment attaches to
     * @param savedInstanceState Saved state of the fragment (if any)
     * @return The inflated fragment view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_transactions, container, false);

        etPaymentAmount = view.findViewById(R.id.etPaymentAmount);
        btnSendPayment = view.findViewById(R.id.btnSendPayment);

        // Initialize Bluetooth manager with empty callback implementation
        BluetoothManager.getInstance(requireContext(), new Bluetooth.BluetoothCallback() {
            @Override public void onDeviceFound(android.bluetooth.BluetoothDevice device) {}
            @Override public void onScanFinished() {}
            @Override public void onScanStarted() {}
            @Override public void onError(String error) {}
            @Override public void onConnectionSuccess(android.bluetooth.BluetoothDevice device) {}
            @Override public void onDataSent(boolean success) {}
            @Override public void onDataReceived(UserData data) {}
            @Override public void onDataReceivedRaw(String message) {}
            @Override public void onDisconnected() {}
        });

        // Set click listener for sending the payment
        btnSendPayment.setOnClickListener(v -> sendPayment());

        return view;
    }

    /**
     * Reads and validates the payment amount, then sends it to the customer.
     * Shows success or failure via Toast messages.
     */
    private void sendPayment() {
        String amountText = etPaymentAmount.getText().toString().trim().replace(",", ".");

        // Check if input is empty
        if (amountText.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error_enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), getString(R.string.error_invalid_amount), Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current Bluetooth connection
        Bluetooth bt = BluetoothManager.getInstance();
        if (bt == null || !bt.isConnected()) {
            Toast.makeText(requireContext(), getString(R.string.error_not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent duplicate taps
        btnSendPayment.setEnabled(false);

        // Send payment in background thread
        new Thread(() -> {
            boolean sent = bt.sendRawMessage("amount:" + amountText);
            requireActivity().runOnUiThread(() -> {
                if (sent) {
                    Toast.makeText(requireContext(), getString(R.string.sent_amount, amountText), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
                }
                btnSendPayment.setEnabled(true); // Re-enable button
            });
        }).start();
    }

    /**
     * Returns the title used in the UI for this fragment.
     *
     * @return A string title
     */
    @Override
    public String getTitle() {
        return "Transactions";
    }
}
