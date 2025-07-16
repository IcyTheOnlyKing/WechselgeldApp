package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.Database.Models.Balance;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class TransactionFragment extends BaseFragment {

    private static final String TAG = "TransactionFragment";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // UI Components
    private TextView tvCustomerBalance, tvInvoiceAmount, tvRemainingAmount, tvSelectedAmount;
    private TextInputEditText etCustomAmount;
    private MaterialButton btnAmount5, btnAmount10, btnAmount20, btnSendPayment;

    // Database
    private DatabaseHelper dbHelper;
    private Balance currentBalance;
    private NumberFormat currencyFormat;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private BluetoothDevice connectedDevice;

    // Transaction data
    private double invoiceAmount = 0.0;
    private double selectedPaymentAmount = 0.0;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_transaction_ui, container, false);

        initializeViews(view);
        setupBluetooth();
        loadCustomerData();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        tvCustomerBalance = view.findViewById(R.id.tvCustomerBalance);
        tvInvoiceAmount = view.findViewById(R.id.tvInvoiceAmount);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);
        tvSelectedAmount = view.findViewById(R.id.tvSelectedAmount);
        etCustomAmount = view.findViewById(R.id.etCustomAmount);
        btnAmount5 = view.findViewById(R.id.btnAmount5);
        btnAmount10 = view.findViewById(R.id.btnAmount10);
        btnAmount20 = view.findViewById(R.id.btnAmount20);
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

        new Thread(() -> {
            try {
                // Connect to paired seller device (replace with actual device address)
                String deviceAddress = "00:11:22:33:44:55";
                connectedDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

                bluetoothSocket = connectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();

                listenForIncomingData();
            } catch (IOException e) {
                Log.e(TAG, "Bluetooth-Verbindungsfehler: " + e.getMessage());
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Bluetooth-Verbindung fehlgeschlagen", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void listenForIncomingData() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (true) {
            try {
                bytes = inputStream.read(buffer);
                String receivedData = new String(buffer, 0, bytes);
                processReceivedData(receivedData);
            } catch (IOException e) {
                Log.e(TAG, "Bluetooth-Lesefehler: " + e.getMessage());
                break;
            }
        }
    }

    private void processReceivedData(String data) {
        try {
            String amountStr = data.split("\"amount\":")[1].replaceAll("[^0-9.]", "");
            invoiceAmount = Double.parseDouble(amountStr);

            requireActivity().runOnUiThread(() -> {
                updateUI();
                Toast.makeText(getContext(), "Rechnung empfangen: " + currencyFormat.format(invoiceAmount),
                        Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Parsen der Daten: " + e.getMessage());
        }
    }

    private void loadCustomerData() {
        currentBalance = dbHelper.getLatestBalance();
        if (currentBalance == null) {
            currentBalance = new Balance(
                    "customer-id",
                    "Kunde",
                    0.0,
                    System.currentTimeMillis() / 1000
            );
            dbHelper.insertBalance(currentBalance);
        }
        updateUI();
    }

    private void setupListeners() {
        btnAmount5.setOnClickListener(v -> setPaymentAmount(5.0));
        btnAmount10.setOnClickListener(v -> setPaymentAmount(10.0));
        btnAmount20.setOnClickListener(v -> setPaymentAmount(20.0));

        // Custom amount input
        etCustomAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double amount = s.length() > 0 ? Double.parseDouble(s.toString()) : 0.0;
                    setPaymentAmount(amount);
                } catch (NumberFormatException e) {
                    setPaymentAmount(0.0);
                }
            }
        });

        // Send payment button
        btnSendPayment.setOnClickListener(v -> processPayment());
    }

    private void setPaymentAmount(double amount) {
        selectedPaymentAmount = amount;
        tvSelectedAmount.setText(currencyFormat.format(amount));
        updatePaymentButtonState();
    }

    private void updateUI() {
        tvCustomerBalance.setText(currencyFormat.format(currentBalance.getBalance()));

        if (invoiceAmount > 0) {
            tvInvoiceAmount.setText(currencyFormat.format(invoiceAmount));
            double remainingAmount = Math.max(0, invoiceAmount - currentBalance.getBalance());
            tvRemainingAmount.setText(currencyFormat.format(remainingAmount));
        }

        updatePaymentButtonState();
    }

    private void updatePaymentButtonState() {
        boolean hasRemainingBalance = invoiceAmount > 0 &&
                (invoiceAmount - currentBalance.getBalance()) > 0;

        btnAmount5.setEnabled(hasRemainingBalance);
        btnAmount10.setEnabled(hasRemainingBalance);
        btnAmount20.setEnabled(hasRemainingBalance);
        etCustomAmount.setEnabled(hasRemainingBalance);
        btnSendPayment.setEnabled(hasRemainingBalance && selectedPaymentAmount > 0);
    }

    private void processPayment() {
        if (selectedPaymentAmount <= 0) {
            Toast.makeText(getContext(), "Bitte Betrag auswählen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate new balance
        double newBalanceValue;
        double amountPaid;

        if (currentBalance.getBalance() >= invoiceAmount) {
            // Full payment from balance
            newBalanceValue = currentBalance.getBalance() - invoiceAmount;
            amountPaid = invoiceAmount;
        } else {
            // Partial payment (balance + cash)
            amountPaid = currentBalance.getBalance() + selectedPaymentAmount;
            newBalanceValue = 0.0;
        }

        // Create new balance record
        Balance newBalance = new Balance(
                currentBalance.getOtherUuidT(),
                currentBalance.getDisplayName(),
                newBalanceValue,
                System.currentTimeMillis() / 1000
        );

        // Update database
        dbHelper.insertBalance(newBalance);
        dbHelper.insertTransaction(amountPaid, System.currentTimeMillis() / 1000);

        // Update current balance and UI
        currentBalance = newBalance;
        updateUI();

        // Send payment confirmation
        sendPaymentConfirmation(amountPaid);

        Toast.makeText(getContext(), "Zahlung erfolgreich", Toast.LENGTH_SHORT).show();
    }

    private void sendPaymentConfirmation(double amountPaid) {
        if (outputStream == null) {
            Toast.makeText(getContext(), "Keine Bluetooth-Verbindung", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String confirmation = String.format(Locale.US,
                    "{\"payment\":%.2f,\"newBalance\":%.2f}",
                    amountPaid, currentBalance.getBalance());

            outputStream.write(confirmation.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Fehler beim Senden der Bestätigung: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Fehler beim Schließen der Verbindung: " + e.getMessage());
        }
    }

    @Override
    public String getTitle() {
        return "Zahlung";
    }
}