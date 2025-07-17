package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.Database.Models.Balance;
import htl.steyr.wechselgeldapp.Database.Models.Transaction;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class TransactionFragment extends BaseFragment {

    private static final String TAG = "TransactionFragment";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private TextView tvCustomerName, tvCustomerBalance, tvRemainingAmount, tvTodayPayment, tvNewBalance;
    private EditText etPaymentAmount;
    private Button btnSendPayment;
    private DatabaseHelper dbHelper;
    private Balance currentBalance;
    private NumberFormat currencyFormat;

    // Bluetooth variables
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private BluetoothDevice connectedDevice;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_transactions, container, false);

        initializeViews(view);
        setupBluetooth();
        loadCustomerData();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvCustomerBalance = view.findViewById(R.id.tvCustomerBalance);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);
        tvTodayPayment = view.findViewById(R.id.tvTodayPayment);
        tvNewBalance = view.findViewById(R.id.tvNewBalance);
        etPaymentAmount = view.findViewById(R.id.etPaymentAmount);
        btnSendPayment = view.findViewById(R.id.btnSendPayment);

        dbHelper = new DatabaseHelper(getContext());
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth nicht unterstützt", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                connectedDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
            }

            bluetoothSocket = connectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Bluetooth Verbindungsfehler: " + e.getMessage());
            Toast.makeText(getContext(), "Bluetooth Verbindung fehlgeschlagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCustomerData() {
        // Test data
        currentBalance = new Balance(
                "uuid-customer-123",
                "Max Mustermann",
                25.50,
                System.currentTimeMillis() / 1000
        );

        tvCustomerName.setText(currentBalance.getDisplayName());
        tvCustomerBalance.setText(currencyFormat.format(currentBalance.getBalance()));
    }

    private void setupListeners() {
        etPaymentAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateRemainingAmount();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSendPayment.setOnClickListener(v -> processPayment());
    }

    private void calculateRemainingAmount() {
        try {
            double paymentAmount = Double.parseDouble(etPaymentAmount.getText().toString());
            // Show the full payment amount as remaining amount (no subtraction)
            tvRemainingAmount.setText(currencyFormat.format(paymentAmount));
        } catch (NumberFormatException e) {
            tvRemainingAmount.setText(currencyFormat.format(0));
        }
    }

    private void processPayment() {
        try {
            double paymentAmount = Double.parseDouble(etPaymentAmount.getText().toString());

            if (paymentAmount <= 0) {
                showToast("Bitte geben Sie einen gültigen Betrag ein");
                return;
            }

            // Send only the payment amount to customer
            sendPaymentData(paymentAmount);

            // Update local database
            dbHelper.insertTransaction(paymentAmount, System.currentTimeMillis() / 1000);

            // Update UI
            updateUI(paymentAmount);

            showToast("Zahlung erfolgreich");

        } catch (NumberFormatException e) {
            showToast("Ungültiger Betrag");
        }
    }

    private void sendPaymentData(double amount) {
        if (outputStream == null) {
            showToast("Keine Bluetooth Verbindung");
            return;
        }

        try {
            // Format data as JSON (only sending the amount)
            String paymentData = String.format(Locale.US,
                    "{\"amount\":%.2f}",
                    amount);

            // Send data
            outputStream.write(paymentData.getBytes());
            outputStream.flush();

        } catch (IOException e) {
            Log.e(TAG, "Bluetooth Sendefehler: " + e.getMessage());
            showToast("Senden fehlgeschlagen");
        }
    }

    private void updateUI(double paymentAmount) {
        tvTodayPayment.setText(currencyFormat.format(paymentAmount));
        tvNewBalance.setText(currencyFormat.format(currentBalance.getBalance()));
        tvCustomerBalance.setText(currencyFormat.format(currentBalance.getBalance()));
        etPaymentAmount.setText("");
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
        ;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Socket Schließfehler: " + e.getMessage());
        }
    }

    @Override
    public String getTitle() {
        return "Zahlungen";
    }
}