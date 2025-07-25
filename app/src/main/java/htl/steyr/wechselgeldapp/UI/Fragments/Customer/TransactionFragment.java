package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import htl.steyr.wechselgeldapp.Bluetooth.Bluetooth;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothDataService;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class TransactionFragment extends BaseFragment {

    // TODO: btnAmount1 , 2 , 3 soll im textfeld stehen

    private TextView tvInvoiceAmount;
    private TextView tvRemainingAmount;
    private TextView tvSelectedAmount;
    private TextView tvAmountToPay;
    private TextView tvCustomerBalance;
    private MaterialButton btnAmount1, btnAmount2, btnAmount3, btnSendPayment;
    private TextInputEditText etCustomAmount;

    private BluetoothDataService dataService;
    private DatabaseHelper dbHelper;

    private double currentBalance = 0;
    private double invoiceAmount = 0;
    private double selectedTip = 0;

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_transaction, container, false);

        tvCustomerBalance = view.findViewById(R.id.tvCustomerBalance);
        tvInvoiceAmount = view.findViewById(R.id.tvInvoiceAmount);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);
        tvSelectedAmount = view.findViewById(R.id.tvSelectedAmount);
        tvAmountToPay = view.findViewById(R.id.tvAmountToPay);
        btnAmount1 = view.findViewById(R.id.btnAmount1);
        btnAmount2 = view.findViewById(R.id.btnAmount2);
        btnAmount3 = view.findViewById(R.id.btnAmount3);
        btnSendPayment = view.findViewById(R.id.btnSendPayment);
        etCustomAmount = view.findViewById(R.id.etCustomAmount);

        tvInvoiceAmount.setText("€0,00");
        tvRemainingAmount.setText("0,00");

        dbHelper = new DatabaseHelper(requireContext());
        int customerId = getCurrentCustomerId();

        if ((dbHelper.getBalanceForCustomer(customerId) == null) || (dbHelper.getBalanceForCustomer(customerId) == 0)){
            currentBalance = 0;
        } else {
            currentBalance = dbHelper.getBalanceForCustomer(customerId);
        }


        tvCustomerBalance.setText(String.format("€%.2f", currentBalance));

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }

        BluetoothManager.getInstance(requireContext(), null).startServer();
        setupButtonListeners();
        tryStartListening();

        return view;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void setupButtonListeners() {
        btnAmount1.setOnClickListener(v -> {
            selectedTip = 1;
            updateCalculatedAmounts();
        });

        btnAmount2.setOnClickListener(v -> {
            selectedTip = 2;
            updateCalculatedAmounts();
        });

        btnAmount3.setOnClickListener(v -> {
            selectedTip = 3;
            updateCalculatedAmounts();
        });

        etCustomAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = etCustomAmount.getText() != null ? etCustomAmount.getText().toString().trim().replace(",", ".") : "";
                if (!input.isEmpty()) {
                    try {
                        selectedTip = Double.parseDouble(input);
                    } catch (NumberFormatException e) {
                        selectedTip = 0;
                    }
                    updateCalculatedAmounts();
                }
            }
        });

        btnSendPayment.setOnClickListener(v -> {
            if (invoiceAmount == 0) {
                Toast.makeText(requireContext(), R.string.no_invoice_received, Toast.LENGTH_SHORT).show();
                return;
            }

            double baseToPay = Math.max(invoiceAmount - currentBalance, 0);
            double amountToPay = baseToPay + selectedTip;

            if (amountToPay > currentBalance) {
                Toast.makeText(requireContext(), R.string.not_enough_balance, Toast.LENGTH_SHORT).show();
                return;
            }

            processPayment(amountToPay);
        });
    }

    @SuppressLint("DefaultLocale")
    private void updateCalculatedAmounts() {
        double baseToPay = Math.max(invoiceAmount - currentBalance, 0);
        double totalAmount = baseToPay + selectedTip;

        tvRemainingAmount.setText(String.format("€%.2f", baseToPay));
        tvSelectedAmount.setText(String.format("€%.2f", selectedTip));
        etCustomAmount.setText(String.format("€%.2f", selectedTip));
        tvAmountToPay.setText(String.format("€%.2f", totalAmount));
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @SuppressLint("DefaultLocale")
    private void processPayment(double amount) {
        double baseToPay = Math.max(invoiceAmount - currentBalance, 0);
        double newBalance = currentBalance - (amount - baseToPay);


        Bluetooth bt = BluetoothManager.getInstance();
        if (bt.isConnected() && bt.getConnectedSocket() != null) {
            String otherUuid = bt.getConnectedSocket().getRemoteDevice().getAddress();  // MAC-Adresse
            String displayName = bt.getConnectedSocket().getRemoteDevice().getName();
            long timestamp = System.currentTimeMillis();

            dbHelper.insertOrUpdateBalance(otherUuid, displayName, newBalance, timestamp);
        }


        currentBalance = newBalance;
        tvCustomerBalance.setText(String.format("€%.2f", newBalance));

        Toast.makeText(requireContext(),
                String.format("Zahlung über €%.2f erfolgreich", amount),
                Toast.LENGTH_SHORT).show();
    }


    private void tryStartListening() {
        Bluetooth bt = BluetoothManager.getInstance();

        if (!bt.isConnected()) {
            new Handler(Looper.getMainLooper()).postDelayed(this::tryStartListening, 1000);
            return;
        }

        Handler handler = createMessageHandler();

        try {
            dataService = new BluetoothDataService(bt.getConnectedSocket(), handler);
            dataService.listenForMessages();
        } catch (IOException e) {
            Log.e("TransactionFragment", "Fehler beim Start des DataService: " + e.getMessage());
        }
    }

    private Handler createMessageHandler() {
        return new Handler(Looper.getMainLooper()) {
            @SuppressLint("DefaultLocale")
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.obj instanceof String) {
                    String message = (String) msg.obj;

                    if (message.startsWith("amount:")) {
                        try {
                            invoiceAmount = Double.parseDouble(message.substring(7).trim());
                        } catch (NumberFormatException e) {
                            Log.e("TransactionFragment", "Ungültiger Betrag: " + message);
                            return;
                        }

                        selectedTip = 0;
                        etCustomAmount.setText("");
                        tvInvoiceAmount.setText(String.format("€%.2f", invoiceAmount));
                        updateCalculatedAmounts();
                    }
                }
            }
        };
    }

    private int getCurrentCustomerId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String displayName = prefs.getString("user_display_name", null);

        if (displayName == null) return -1;

        String customerIdStr = dbHelper.getCustomerIdByName(displayName);

        try {
            return Integer.parseInt(customerIdStr);
        } catch (NumberFormatException e) {
            return -1;
        }
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
        return "Zahlung";
    }
}
