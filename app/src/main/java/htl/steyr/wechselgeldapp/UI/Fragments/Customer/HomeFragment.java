package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.Bluetooth.TransactionRequest;
import htl.steyr.wechselgeldapp.Backup.UserData;
import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;
import htl.steyr.wechselgeldapp.Utilities.Security.SessionManager;

public class HomeFragment extends BaseFragment implements Bluetooth.BluetoothCallback {

    private TextView tvBalance;
    private DatabaseHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_home, container, false);
        tvBalance = view.findViewById(R.id.tvCustomerBalance);
        dbHelper = new DatabaseHelper(requireContext());

        // Bluetooth-Callback registrieren
        BluetoothManager.getInstance().setCallback(this);

        // Guthaben anzeigen
        updateBalanceDisplay();
        return view;
    }

    private void updateBalanceDisplay() {
        String uuid = SessionManager.getCurrentUserUuid(requireContext());
        if (uuid != null) {
            double balance = dbHelper.getBalanceForUuid(uuid);
            tvBalance.setText(String.format("€%.2f", balance));
        } else {
            Toast.makeText(requireContext(), "Benutzer nicht angemeldet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTransactionReceived(TransactionRequest transaction) {
        requireActivity().runOnUiThread(() -> {
            // Guthaben aktualisieren
            dbHelper.updateBalance(transaction.getReceiverUuid(), -transaction.getAmount());
            updateBalanceDisplay();

            // Benachrichtigung anzeigen
            Toast.makeText(
                    requireContext(),
                    "Zahlung über €" + transaction.getAmount() + " erhalten",
                    Toast.LENGTH_LONG
            ).show();
        });
    }

    @Override public void onDeviceFound(BluetoothDevice device) {}
    @Override public void onScanFinished() {}
    @Override public void onScanStarted() {}
    @Override public void onError(String error) {}
    @Override public void onConnectionSuccess(BluetoothDevice device) {}
    @Override public void onDataSent(boolean success) {}
    @Override public void onDataReceived(UserData data) {}
    @Override public void onDisconnected() {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Callback entfernen um Memory Leaks zu vermeiden
        BluetoothManager.setCallback(null);
    }

    @Override
    public String getTitle() {
        return "Wechselgeld App";
    }
}