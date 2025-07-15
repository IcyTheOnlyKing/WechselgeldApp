package htl.steyr.wechselgeldapp.UI.Fragments.Customer;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Locale;

import htl.steyr.wechselgeldapp.Database.DatabaseHelper;
import htl.steyr.wechselgeldapp.R;
import htl.steyr.wechselgeldapp.UI.Fragments.BaseFragment;

public class HomeFragment extends BaseFragment {

    private DatabaseHelper dbHelper;
    private String currentOtherUuid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.customer_fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UUID aus Argumenten holen
        Bundle args = getArguments();
        if (args != null) {
            currentOtherUuid = args.getString("UUID", "demo-uuid");
        } else {
            currentOtherUuid = "demo-uuid";
        }

        dbHelper = new DatabaseHelper(requireContext());


    }

    private void loadBalanceData(TextView textView) {
        try (Cursor balanceCursor = dbHelper.getBalanceForUuid(currentOtherUuid)) {
            String balanceText = "0,00 €";
            if (balanceCursor != null && balanceCursor.moveToFirst()) {
                double balance = balanceCursor.getDouble(0);
                balanceText = String.format(Locale.getDefault(), "%.2f €", balance);
            }
            textView.setText(balanceText);
        } catch (Exception e) {
            textView.setText("Fehler");
        }
    }

    private void loadTransactionData(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();

        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM Transactions WHERE timestamp >= ? AND timestamp < ?",
                new String[]{String.valueOf(startOfDay), String.valueOf(endOfDay)}
        )) {
            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            textView.setText(String.valueOf(count));
        } catch (Exception e) {
            textView.setText("0");
        }
    }

    @Override
    public void onDestroyView() {
        dbHelper.close();
        super.onDestroyView();
    }

    @Override
    public String getTitle() {
        return "Wechselgeld App";
    }
}