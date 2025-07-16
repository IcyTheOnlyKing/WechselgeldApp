package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import htl.steyr.wechselgeldapp.Backup.UserData;
import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class TransactionFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_transactions, container, false);

        Button sendButton = view.findViewById(R.id.btnSendPayment);
        sendButton.setOnClickListener(v -> sendDemoData());

        return view;
    }

    private void sendDemoData() {
        Bluetooth bluetooth = BluetoothManager.getInstance();
        if (bluetooth != null && bluetooth.isConnected()) {
            UserData data = new UserData();
            data.setUsername("Demo Benutzer");
            data.setTotalAmount(42.0);
            bluetooth.sendUserData(data);
        }
    }

    @Override
    public String getTitle() {
        return "Transaktionen";
    }
}