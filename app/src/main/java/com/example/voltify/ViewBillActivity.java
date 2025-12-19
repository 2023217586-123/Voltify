package com.example.voltify;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ViewBillActivity extends AppCompatActivity {

    TextView textMonth, textUnits, textTotal, textRebate, textFinal;
    Button buttonBack;
    DataHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bill);

        textMonth = findViewById(R.id.textViewMonth);
        textUnits = findViewById(R.id.textViewUnits);
        textTotal = findViewById(R.id.textViewTotal);
        textRebate = findViewById(R.id.textViewRebate);
        textFinal = findViewById(R.id.textViewFinal);
        buttonBack = findViewById(R.id.buttonBack);

        dbHelper = new DataHelper(this);

        int billId = getIntent().getIntExtra("bill_id", -1);

        if (billId != -1) {
            loadBillDetails(billId);
        }

        buttonBack.setOnClickListener(v -> finish());
    }

    private void loadBillDetails(int billId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT month, units, total, rebate, final FROM bill WHERE id=?",
                new String[]{String.valueOf(billId)}
        );

        if (cursor.moveToFirst()) {
            String month = cursor.getString(0);
            int units = cursor.getInt(1);
            double total = cursor.getDouble(2);
            double rebate = cursor.getDouble(3);
            double finalCost = cursor.getDouble(4);

            textMonth.setText("Month: " + month);
            textUnits.setText("Units (kWh): " + units);
            textTotal.setText("Total charges: RM " + String.format(Locale.US, "%.2f", total));
            textRebate.setText("Rebate: " + rebate + "%");
            textFinal.setText("Final cost: RM " + String.format(Locale.US, "%.2f", finalCost));
        }

        cursor.close();
    }
}
