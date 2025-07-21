package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.Bluetooth.TransactionRequest;
import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;
import htl.steyr.wechselgeldapp.Utilities.Security.SessionManager;

public class TransactionFragment extends BaseFragment {
    private EditText etAmount;
    private TextView tvRemainingAmount;
    private Button btnSendPayment;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_transactions, container, false);

        etAmount = view.findViewById(R.id.etPaymentAmount);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);
        btnSendPayment = view.findViewById(R.id.btnSendPayment);
        dbHelper = new DatabaseHelper(requireContext());

        btnSendPayment.setOnClickListener(v -> sendTransaction());

        return view;
    }

    private void sendTransaction() {
        try {
            double amount = Double.parseDouble(etAmount.getText().toString());
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Ungültiger Betrag", Toast.LENGTH_SHORT).show();
                return;
            }

            Context context = requireContext();
            String sellerUuid = SessionManager.getCurrentUserUuid(context);
            String customerUuid = dbHelper.getPairedCustomerUuid(sellerUuid);

            if (customerUuid == null) {
                Toast.makeText(context, "Kein gekoppelter Kunde", Toast.LENGTH_SHORT).show();
                return;
            }

            TransactionRequest transaction = new TransactionRequest(
                    sellerUuid,
                    customerUuid,
                    amount
            );

            BluetoothManager.getInstance().sendTransaction(transaction);

            // Lokale Datenbank aktualisieren
            dbHelper.updateBalance(customerUuid, -amount);

            Toast.makeText(
                    context,
                    "Transaktion gesendet: €" + amount,
                    Toast.LENGTH_SHORT
            ).show();

            etAmount.setText("");

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Ungültige Eingabe", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public String getTitle() {
        return "Transaktionen";
    }

    public TextView getTvRemainingAmount() {
        return tvRemainingAmount;
    }

    public void setTvRemainingAmount(TextView tvRemainingAmount) {
        this.tvRemainingAmount = tvRemainingAmount;
    }
}