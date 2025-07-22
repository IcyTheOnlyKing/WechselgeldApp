package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothDataService;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class TransactionFragment extends BaseFragment {

    private TextView tvInvoiceAmount, tvRemainingAmount;
    private BluetoothDataService dataService;

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

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }

        // Callback zur Vermeidung von NullPointer
        Bluetooth bluetooth = BluetoothManager.getInstance(requireContext(), btCallback);
        bluetooth.setCallback(btCallback);
        bluetooth.startServer();

        tryStartListening();

        return view;
    }

    private void tryStartListening() {
        if (BluetoothManager.getInstance().getConnectedSocket() == null) {
            return;
        }

        Handler handler = createMessageHandler();
        try {
            dataService = new BluetoothDataService(
                    BluetoothManager.getInstance().getConnectedSocket(), handler);
            dataService.listenForMessages();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Bluetooth Empfang fehlgeschlagen", Toast.LENGTH_SHORT).show();
        }
    }

    private final Bluetooth.BluetoothCallback btCallback = new Bluetooth.BluetoothCallback() {
        @Override public void onDeviceFound(android.bluetooth.BluetoothDevice device) {}
        @Override public void onScanFinished() {}
        @Override public void onScanStarted() {}
        @Override public void onError(String error) {
            Toast.makeText(requireContext(), "Bluetooth-Fehler: " + error, Toast.LENGTH_SHORT).show();
        }
        @Override public void onConnectionSuccess(android.bluetooth.BluetoothDevice device) {}
        @Override public void onDataSent(boolean success) {}
        @Override public void onDataReceivedRaw(String message) {
            if (message.startsWith("amount:")) {
                String amount = message.substring(7).trim();
                tvInvoiceAmount.setText("€" + amount);
                tvRemainingAmount.setText("€" + amount);
            }
        }
        @Override public void onDataReceived(htl.steyr.wechselgeldapp.Backup.UserData data) {}
        @Override public void onDisconnected() {}
    };

    private Handler createMessageHandler() {
        return new Handler(Looper.getMainLooper()) {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.obj instanceof String) {
                    String message = (String) msg.obj;
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
