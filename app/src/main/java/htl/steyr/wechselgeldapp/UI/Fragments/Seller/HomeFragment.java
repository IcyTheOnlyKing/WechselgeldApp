package htl.steyr.wechselgeldapp.UI.Fragments.Seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.Database.Models.Device;
import htl.steyr.wechselgeldapp.Database.Models.Transaction;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class HomeFragment extends BaseFragment {

    // Column name constants
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_UUID = "uuid";
    private static final String COLUMN_CUSTOMER_ID = "customerId";
    private static final String COLUMN_SELLER_ID = "sellerId";
    private static final String COLUMN_DEVICE_NAME = "deviceName";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    private TextView lastTransactionAmount, username, transactionAmount;
    private RecyclerView deviceRecyclerView;
    private DeviceAdapter deviceAdapter;
    private DatabaseHelper dbHelper;
    private NumberFormat currencyFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_fragment_home, container, false);

        // Initialize views
        lastTransactionAmount = view.findViewById(R.id.lastTransactionAmount);
        username = view.findViewById(R.id.username);
        transactionAmount = view.findViewById(R.id.transactionAmount);
        deviceRecyclerView = view.findViewById(R.id.device_list);

        // Initialize database helper and formatters
        dbHelper = new DatabaseHelper(getContext());
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

        // Setup device list
        setupDeviceList();

        // Load transaction data
        loadTransactionData();

        return view;
    }

    private void setupDeviceList() {
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceAdapter = new DeviceAdapter();
        deviceRecyclerView.setAdapter(deviceAdapter);

        // Load devices from database
        List<Device> devices = getConnectedDevices();
        deviceAdapter.setDevices(devices);
    }

    private List<Device> getConnectedDevices() {
        List<Device> devices = new ArrayList<>();
        android.database.Cursor cursor = dbHelper.getAllDevices();

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int uuidIndex = cursor.getColumnIndex(COLUMN_UUID);
            int customerIdIndex = cursor.getColumnIndex(COLUMN_CUSTOMER_ID);
            int sellerIdIndex = cursor.getColumnIndex(COLUMN_SELLER_ID);
            int deviceNameIndex = cursor.getColumnIndex(COLUMN_DEVICE_NAME);

            // Only proceed if all required columns exist
            if (idIndex >= 0 && uuidIndex >= 0 && deviceNameIndex >= 0) {
                do {
                    Device device = new Device(
                            cursor.getInt(idIndex),
                            cursor.getString(uuidIndex),
                            customerIdIndex >= 0 && !cursor.isNull(customerIdIndex) ? cursor.getInt(customerIdIndex) : null,
                            sellerIdIndex >= 0 && !cursor.isNull(sellerIdIndex) ? cursor.getInt(sellerIdIndex) : null,
                            cursor.getString(deviceNameIndex)
                    );
                    devices.add(device);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return devices;
    }

    private void loadTransactionData() {
        android.database.Cursor lastTransactionCursor = dbHelper.getAllTransactions();
        if (lastTransactionCursor != null && lastTransactionCursor.moveToFirst()) {
            int idIndex = lastTransactionCursor.getColumnIndex(COLUMN_ID);
            int amountIndex = lastTransactionCursor.getColumnIndex(COLUMN_AMOUNT);
            int timestampIndex = lastTransactionCursor.getColumnIndex(COLUMN_TIMESTAMP);

            if (amountIndex >= 0 && timestampIndex >= 0) {
                Transaction lastTransaction = new Transaction(
                        idIndex >= 0 ? lastTransactionCursor.getInt(idIndex) : 0,
                        lastTransactionCursor.getDouble(amountIndex),
                        lastTransactionCursor.getLong(timestampIndex)
                );

                lastTransactionAmount.setText(currencyFormat.format(lastTransaction.amount));
                username.setText(getShopName());

                // Count today's transactions
                long todayStart = getStartOfDayTimestamp();
                int todayCount = 0;
                double todayTotal = 0;

                do {
                    long timestamp = lastTransactionCursor.getLong(timestampIndex);
                    if (timestamp >= todayStart) {
                        todayCount++;
                        todayTotal += lastTransactionCursor.getDouble(amountIndex);
                    }
                } while (lastTransactionCursor.moveToNext());

                transactionAmount.setText(String.valueOf(todayCount));
            }
        }
        if (lastTransactionCursor != null) {
            lastTransactionCursor.close();
        }
    }

    private long getStartOfDayTimestamp() {
        Date date = new Date();
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        return date.getTime() / 1000;
    }

    private String getShopName() {
        return dbHelper.getShopName();
    }

    @Override
    public String getTitle() {
        return "Übersicht";
    }

    // Device Adapter for RecyclerView
    private class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
        private List<Device> devices = new ArrayList<>();

        public void setDevices(List<Device> devices) {
            this.devices = devices;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new DeviceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
            Device device = devices.get(position);
            holder.deviceName.setText(device.deviceName);

            holder.itemView.setOnClickListener(v -> {
                // Navigate to connection page with device info (Nice-To-Have Feature)
            });
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        class DeviceViewHolder extends RecyclerView.ViewHolder {
            TextView deviceName;

            public DeviceViewHolder(@NonNull View itemView) {
                super(itemView);
                deviceName = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}