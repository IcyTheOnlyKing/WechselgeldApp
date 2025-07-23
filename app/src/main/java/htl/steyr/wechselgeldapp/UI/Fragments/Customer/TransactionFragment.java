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

import java.io.IOException;

import htl.steyr.wechselgeldapp.Bluetooth.BluetoothDataService;
import htl.steyr.wechselgeldapp.Bluetooth.BluetoothManager;
import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

/**
 * Fragment for the customer to receive transaction data via Bluetooth and handle payments.
 */
public class TransactionFragment extends BaseFragment {

    private TextView tvCustomerBalance, tvInvoiceAmount, tvRemainingAmount, tvSelectedAmount, tvAmountToPay;
    private MaterialButton btnAmount1, btnAmount2, btnAmount3, btnSendPayment;
    private BluetoothDataService dataService;
    private DatabaseHelper dbHelper;

    private double currentBalance = 0;
    private double invoiceAmount = 0;
    private double remainingAmount = 0;
    private double selectedTip = 0;

    /**
     * Initializes the fragment, UI elements, Bluetooth server, and database connection.
     *
     * @param inflater LayoutInflater to inflate the view
     * @param container ViewGroup container
     * @param savedInstanceState previously saved state
     * @return the inflated and initialized view
     */
    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_transaction, container, false);

        // Initialize UI components
        tvCustomerBalance = view.findViewById(R.id.tvCustomerBalance);
        tvInvoiceAmount = view.findViewById(R.id.tvInvoiceAmount);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);
        tvSelectedAmount = view.findViewById(R.id.tvSelectedAmount);
        tvAmountToPay = view.findViewById(R.id.tvAmountToPay);
        btnAmount1 = view.findViewById(R.id.btnAmount1);
        btnAmount2 = view.findViewById(R.id.btnAmount2);
        btnAmount3 = view.findViewById(R.id.btnAmount3);
        btnSendPayment = view.findViewById(R.id.btnSendPayment);

        // Initialize database helper
        dbHelper = new DatabaseHelper(requireContext());

        // Load and display current customer balance
        int customerId = getCurrentCustomerId();
        currentBalance = dbHelper.getBalanceForCustomer(customerId);
        tvCustomerBalance.setText(String.format("€%.2f", currentBalance));

        // Make the device discoverable via Bluetooth
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }

        // Start Bluetooth server for incoming connections
        BluetoothManager.getInstance(requireContext(), null).startServer();

        // Register button listeners
        setupButtonListeners();

        // Prepare to receive data
        tryStartListening();

        return view;
    }

    /**
     * Sets up the click listeners for tip buttons and payment button.
     */
    private void setupButtonListeners() {
        // Tip buttons - add 1€, 2€, or 3€ to the rounded remaining amount
        btnAmount1.setOnClickListener(v -> {
            double roundedAmount = Math.ceil(remainingAmount);
            selectedTip = 1;
            updateSelectedAmount(roundedAmount + selectedTip);
        });

        btnAmount2.setOnClickListener(v -> {
            double roundedAmount = Math.ceil(remainingAmount);
            selectedTip = 2;
            updateSelectedAmount(roundedAmount + selectedTip);
        });

        btnAmount3.setOnClickListener(v -> {
            double roundedAmount = Math.ceil(remainingAmount);
            selectedTip = 3;
            updateSelectedAmount(roundedAmount + selectedTip);
        });

        // Send payment button
        btnSendPayment.setOnClickListener(v -> {
            if (invoiceAmount == 0) {
                Toast.makeText(requireContext(), "Kein Rechnungsbetrag empfangen", Toast.LENGTH_SHORT).show();
                return;
            }

            double amountToPay = remainingAmount + selectedTip;
            if (amountToPay > currentBalance) {
                Toast.makeText(requireContext(), "Nicht genug Guthaben", Toast.LENGTH_SHORT).show();
                return;
            }

            // Process the payment
            processPayment(amountToPay);
        });
    }

    /**
     * Updates the selected amount and displays it in the UI.
     *
     * @param amount the total amount to be paid including tip
     */
    private void updateSelectedAmount(double amount) {
        tvSelectedAmount.setText(String.format("€%.2f", amount));
        tvAmountToPay.setText(String.format("€%.2f", amount));
    }

    /**
     * Processes the payment by deducting the balance, sending confirmation, etc.
     *
     * @param amount amount to be paid
     */
    private void processPayment(double amount) {
        // Placeholder logic for actual payment processing
        Toast.makeText(requireContext(),
                String.format("Zahlung über €%.2f erfolgreich", amount),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Attempts to start the Bluetooth data service for receiving messages.
     */
    private void tryStartListening() {
        if (BluetoothManager.getInstance().getConnectedSocket() == null) {
            Log.w("TransactionFragment", "No Bluetooth socket connected - waiting for connection.");
            return;
        }

        Handler handler = createMessageHandler();

        try {
            dataService = new BluetoothDataService(
                    BluetoothManager.getInstance().getConnectedSocket(), handler);
            dataService.listenForMessages();
            Log.d("TransactionFragment", "BluetoothDataService started");
        } catch (IOException e) {
            Log.e("TransactionFragment", "Error starting DataService: " + e.getMessage());
        }
    }

    /**
     * Creates a handler for processing incoming Bluetooth messages.
     *
     * @return the message handler
     */
    private Handler createMessageHandler() {
        return new Handler(Looper.getMainLooper()) {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.obj instanceof String) {
                    String message = (String) msg.obj;
                    Log.d("TransactionFragment", "Received: " + message);

                    if (message.startsWith("amount:")) {
                        invoiceAmount = Double.parseDouble(message.substring(7).trim());
                        remainingAmount = invoiceAmount - currentBalance;

                        // Update UI with received invoice amount
                        tvInvoiceAmount.setText(String.format("€%.2f", invoiceAmount));
                        tvRemainingAmount.setText(String.format("€%.2f", remainingAmount));
                        tvAmountToPay.setText(String.format("€%.2f", remainingAmount));

                        // Reset tip selection
                        selectedTip = 0;
                        tvSelectedAmount.setText("€0,00");
                    }
                }
            }
        };
    }

    /**
     * Retrieves the current logged-in customer's ID based on shared preferences.
     *
     * @return the customer ID or -1 if invalid
     */
    private int getCurrentCustomerId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String displayName = prefs.getString("user_display_name", null);

        if (displayName == null) {
            Log.e("TransactionFragment", "No user logged in");
            return -1;
        }

        String customerIdStr = dbHelper.getCustomerIdByName(displayName);

        try {
            return Integer.parseInt(customerIdStr);
        } catch (NumberFormatException e) {
            Log.e("TransactionFragment", "Invalid customer ID format: " + customerIdStr);
            return -1;
        }
    }

    /**
     * Stops the Bluetooth data service when the fragment view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dataService != null) {
            dataService.stop();
        }
    }

    /**
     * Returns the title to be displayed for this fragment.
     *
     * @return title string
     */
    @Override
    public String getTitle() {
        return "Zahlung";
    }
}
