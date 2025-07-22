package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import java.io.IOException;

import htl.steyr.wechselgeldapp.Bluetooth.BluetoothDataService;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

/**
 * Fragment für den Kunden zum Empfangen von Transaktionsdaten via Bluetooth.
 */
public class TransactionFragment extends BaseFragment {

    private TextView tvInvoiceAmount, tvRemainingAmount;
    private BluetoothDataService dataService;

    /**
     * Initialisiert das Fragment und macht das Gerät discoverable für den Verkäufer.
     */
    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_transaction, container, false);

        tvInvoiceAmount = view.findViewById(R.id.tvInvoiceAmount);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);

        // Gerät discoverable machen (sichtbar für andere)
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }

        // Bluetooth-Server starten
        BluetoothManager.getInstance(requireContext(), null).startServer();

        // Nachrichtenempfang vorbereiten
        tryStartListening();

        return view;
    }

    /**
     * Startet den BluetoothDataService, wenn eine Verbindung besteht.
     */
    private void tryStartListening() {
        if (BluetoothManager.getInstance().getConnectedSocket() == null) {
            Log.w("TransactionFragment", "Kein Bluetooth-Socket verbunden – warte auf Verbindung.");
            return;
        }

        Handler handler = createMessageHandler();

        try {
            dataService = new BluetoothDataService(
                    BluetoothManager.getInstance().getConnectedSocket(), handler);
            dataService.listenForMessages();
            Log.d("TransactionFragment", "BluetoothDataService gestartet");
        } catch (IOException e) {
            Log.e("TransactionFragment", "Fehler beim Start des DataService: " + e.getMessage());
        }
    }

    /**
     * Erstellt einen Handler, um empfangene Nachrichten zu verarbeiten.
     */
    private Handler createMessageHandler() {
        return new Handler(Looper.getMainLooper()) {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.obj instanceof String) {
                    String message = (String) msg.obj;
                    Log.d("TransactionFragment", "Empfangen: " + message);

                    if (message.startsWith("amount:")) {
                        String amount = message.substring(7).trim();
                        tvInvoiceAmount.setText("€" + amount);
                        tvRemainingAmount.setText("€" + amount);
                    }
                }
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataService != null) {
            dataService.stop();
        }
    }

    @Override
    public String getTitle() {
        return "Transaktionen";
    }
}
