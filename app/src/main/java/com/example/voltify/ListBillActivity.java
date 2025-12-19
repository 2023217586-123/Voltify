package com.example.voltify;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ListBillActivity extends AppCompatActivity {

    ListView listViewBill;
    DataHelper dbHelper;

    String[] billData;   // stores "Month - RM xx.xx"
    int[] billIds;       // stores database IDs (for delete & view)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_bill);

        listViewBill = findViewById(R.id.listViewBill);
        dbHelper = new DataHelper(this);

        loadBills();

        listViewBill.setOnItemClickListener((parent, view, position, id) -> {
            final int selectedBillId = billIds[position];
            final String selectedItem = billData[position];

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Action");
            builder.setItems(new CharSequence[]{"View Details", "Delete Bill"},
                    (dialog, which) -> {
                        if (which == 0) {
                            // View details
                            Intent intent = new Intent(ListBillActivity.this, ViewBillActivity.class);
                            intent.putExtra("bill_id", selectedBillId);
                            startActivity(intent);
                        } else {
                            // Delete bill
                            deleteBill(selectedBillId);
                        }
                    });
            builder.show();
        });
    }

    private void loadBills() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, month, final FROM bill ORDER BY id DESC",
                null
        );

        billData = new String[cursor.getCount()];
        billIds = new int[cursor.getCount()];

        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                billIds[i] = cursor.getInt(0);
                String month = cursor.getString(1);
                double finalCost = cursor.getDouble(2);

                billData[i] = month + " - RM " +
                        String.format(Locale.US, "%.2f", finalCost);
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.list_item_bill,
                billData
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater()
                            .inflate(R.layout.list_item_bill, parent, false);
                }

                TextView textMonth = convertView.findViewById(R.id.textMonth);
                TextView textAmount = convertView.findViewById(R.id.textAmount);

                String[] parts = billData[position].split(" - ");

                textMonth.setText(parts[0]);
                textAmount.setText(parts[1]);

                return convertView;
            }
        };

        listViewBill.setAdapter(adapter);
    }

    private void deleteBill(int billId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("bill", "id=?", new String[]{String.valueOf(billId)});
        Toast.makeText(this, "Bill deleted", Toast.LENGTH_SHORT).show();
        loadBills(); // refresh list
    }
}
