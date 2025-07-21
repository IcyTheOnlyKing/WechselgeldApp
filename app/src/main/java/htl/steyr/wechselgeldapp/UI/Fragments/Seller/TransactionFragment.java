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

public class TransactionFragment extends BaseFragment {

    private EditText etPaymentAmount;
    private MaterialButton btnSendPayment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_transactions, container, false);

        etPaymentAmount = view.findViewById(R.id.etPaymentAmount);
        btnSendPayment = view.findViewById(R.id.btnSendPayment);

        btnSendPayment.setOnClickListener(v -> {
            String amount = etPaymentAmount.getText().toString().trim();
            if (amount.isEmpty()) {
                Toast.makeText(getContext(), "Bitte Betrag eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            Bluetooth bluetooth = BluetoothManager.getInstance();
            if (bluetooth != null && bluetooth.isConnected()) {
                bluetooth.sendRawMessage("amount:" + amount);
                Toast.makeText(getContext(), "Betrag gesendet: €" + amount, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Keine aktive Bluetooth-Verbindung", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public String getTitle() {
        return "Transaktionen";
    }
}
