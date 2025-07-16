package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;

import htl.steyr.wechselgeldapp.Backup.UserData;
import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class TransactionFragment extends BaseFragment {
    private TextView customerNameText;
    private TextView customerBalanceText;
    private TextView newBalanceText;
    private TextView todayPaymentText;
    private TextInputEditText paymentAmountInput;

    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_transactions, container, false);

        customerNameText = view.findViewById(R.id.tvCustomerName);
        customerBalanceText = view.findViewById(R.id.tvCustomerBalance);
        newBalanceText = view.findViewById(R.id.tvNewBalance);
        todayPaymentText = view.findViewById(R.id.tvTodayPayment);
        paymentAmountInput = view.findViewById(R.id.etPaymentAmount);

        dbHelper = new DatabaseHelper(requireContext());

        Button sendButton = view.findViewById(R.id.btnSendPayment);
        sendButton.setOnClickListener(v -> sendTransactionData());

        return view;
    }

    private void sendTransactionData() {
        Bluetooth bluetooth = BluetoothManager.getInstance();
        if (bluetooth != null && bluetooth.isConnected()) {
            String username = customerNameText.getText().toString();
            String balanceStr = customerBalanceText.getText().toString()
                    .replace("€", "").replace(",", ".");
            double currentBalance = 0.0;
            try {
                currentBalance = Double.parseDouble(balanceStr);
            } catch (NumberFormatException ignored) {}

            String amountStr = paymentAmountInput.getText() != null
                    ? paymentAmountInput.getText().toString().replace(",", ".")
                    : "";
            double amount = 0.0;
            if (!amountStr.isEmpty()) {
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException ignored) {}
            }

            double newBalance = currentBalance - amount;
            newBalanceText.setText(String.format(java.util.Locale.getDefault(), "€%.2f", newBalance));
            todayPaymentText.setText(String.format(java.util.Locale.getDefault(), "€%.2f", amount));

            UserData data = new UserData();
            data.setUsername(username);
            data.setSellerName(dbHelper.getShopName());
            data.setTransactionAmount(amount);
            data.setTotalAmount(newBalance);
            bluetooth.sendUserData(data);
        }
    }

    @Override
    public String getTitle() {
        return "Transaktionen";
    }
}