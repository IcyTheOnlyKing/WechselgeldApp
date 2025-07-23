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
 * Fragment for the seller to enter and send a payment amount to the connected customer via Bluetooth.
 */
public class TransactionFragment extends BaseFragment {

    private EditText etPaymentAmount;
    private MaterialButton btnSendPayment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_transactions, container, false);

        etPaymentAmount = view.findViewById(R.id.etPaymentAmount);
        btnSendPayment = view.findViewById(R.id.btnSendPayment);

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

        btnSendPayment.setOnClickListener(v -> sendPayment());

        return view;
    }

    private void sendPayment() {
        String amountText = etPaymentAmount.getText().toString().trim().replace(",", ".");

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

        Bluetooth bt = BluetoothManager.getInstance();
        if (bt == null || !bt.isConnected()) {
            Toast.makeText(requireContext(), getString(R.string.error_not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendPayment.setEnabled(false); // optional: temporär deaktivieren

        new Thread(() -> {
            boolean sent = bt.sendRawMessage("amount:" + amountText);
            requireActivity().runOnUiThread(() -> {
                if (sent) {
                    Toast.makeText(requireContext(), getString(R.string.sent_amount, amountText), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
                }
                btnSendPayment.setEnabled(true);
            });
        }).start();
    }

    @Override
    public String getTitle() {
        return "Transactions";
    }
}
