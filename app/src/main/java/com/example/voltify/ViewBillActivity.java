package com.example.voltify;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ViewBillActivity extends AppCompatActivity {

    TextView txtMonth, txtUnits, txtTotal, txtRebate, txtFinal;
    Button btnBack;
    DataHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bill);

        // Bind views
        txtMonth = findViewById(R.id.textViewMonth);
        txtUnits = findViewById(R.id.textViewUnits);
        txtTotal = findViewById(R.id.textViewTotal);
        txtRebate = findViewById(R.id.textViewRebate);
        txtFinal = findViewById(R.id.textViewFinal);
        btnBack = findViewById(R.id.buttonBack);

        dbHelper = new DataHelper(this);

        // Get ID from intent
        int id = getIntent().getIntExtra("id", -1);
        if (id == -1) {
            finish(); // safety
            return;
        }

        // Query database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT month, units, total, rebate, final FROM bill WHERE id = ?",
                new String[]{String.valueOf(id)}
        );

        if (cursor.moveToFirst()) {
            txtMonth.setText(cursor.getString(0));
            txtUnits.setText(String.valueOf(cursor.getInt(1)));
            txtTotal.setText(String.format(Locale.US, "RM %.2f", cursor.getDouble(2)));
            txtRebate.setText(String.format(Locale.US, "%.2f %%", cursor.getDouble(3)));
            txtFinal.setText(String.format(Locale.US, "RM %.2f", cursor.getDouble(4)));
        }

        cursor.close();

        btnBack.setOnClickListener(v -> finish());
    }
}


