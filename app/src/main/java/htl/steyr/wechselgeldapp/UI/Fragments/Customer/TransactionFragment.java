package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

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

public class TransactionFragment extends BaseFragment {

    private TextView tvInvoiceAmount, tvRemainingAmount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_transaction, container, false);

        tvInvoiceAmount = view.findViewById(R.id.tvInvoiceAmount);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);

        if (BluetoothManager.getInstance().getConnectedSocket() != null) {
            Handler handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    String message = (String) msg.obj;
                    if (message.startsWith("amount:")) {
                        String amount = message.substring(7); // "amount:12.34"
                        tvInvoiceAmount.setText("€" + amount);
                        tvRemainingAmount.setText("€" + amount); // z. B. kein Guthaben abgezogen
                    }
                }
            };

            try {
                BluetoothDataService service = new BluetoothDataService(
                        BluetoothManager.getInstance().getConnectedSocket(), handler);
                service.listenForMessages();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    @Override
    public String getTitle() {
        return "Transaktionen";
    }
}
