package com.example.voltify;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerMonth;
    EditText editUnits, editRebate;
    Button buttonCalc, buttonViewBills, buttonAbout;
    TextView textTotal, textFinal;

    DataHelper dbHelper;

    String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind views
        spinnerMonth = findViewById(R.id.spinnerMonth);
        editUnits = findViewById(R.id.editUnits);
        editRebate = findViewById(R.id.editRebate);
        buttonCalc = findViewById(R.id.buttonCalc);
        buttonViewBills = findViewById(R.id.buttonViewBills);
        buttonAbout = findViewById(R.id.buttonAbout);
        textTotal = findViewById(R.id.textTotal);
        textFinal = findViewById(R.id.textFinal);

        // Spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                months
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        dbHelper = new DataHelper(this);

        // Calculate & Save
        buttonCalc.setOnClickListener(v -> calculateAndSave());

        // Go to list bill page
        buttonViewBills.setOnClickListener(v ->
                startActivity(new Intent(this, ListBillActivity.class))
        );

        // Go to about page
        buttonAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class))
        );
    }

    private void calculateAndSave() {

        String month = spinnerMonth.getSelectedItem().toString();
        String unitsStr = editUnits.getText().toString().trim();
        String rebateStr = editRebate.getText().toString().trim();

        // Validation
        if (unitsStr.isEmpty()) {
            editUnits.setError("Please enter electricity usage");
            return;
        }

        if (rebateStr.isEmpty()) {
            rebateStr = "0";
        }

        int units;
        double rebate;

        try {
            units = Integer.parseInt(unitsStr);
            rebate = Double.parseDouble(rebateStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            return;
        }

        if (units < 0) {
            editUnits.setError("Units cannot be negative");
            return;
        }

        if (rebate < 0 || rebate > 5) {
            editRebate.setError("Rebate must be between 0 and 5%");
            return;
        }

        // Calculation
        double total = calculateTotal(units);
        double finalCost = total - (total * rebate / 100);

        // Display result
        textTotal.setText(
                String.format(Locale.US, "Total charges: RM %.2f", total)
        );
        textFinal.setText(
                String.format(Locale.US, "Final cost (after rebate): RM %.2f", finalCost)
        );

        // Save to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("month", month);
        cv.put("units", units);
        cv.put("total", total);
        cv.put("rebate", rebate);
        cv.put("final", finalCost);

        db.insert("bill", null, cv);

        Toast.makeText(this, "Bill saved successfully", Toast.LENGTH_SHORT).show();

        // Clear input
        editUnits.setText("");
        editRebate.setText("");
    }

    // Block tariff calculation
    private double calculateTotal(int units) {

        double total = 0;

        if (units <= 200) {
            total = units * 0.218;
        } else if (units <= 300) {
            total = (200 * 0.218) + ((units - 200) * 0.334);
        } else if (units <= 600) {
            total = (200 * 0.218) + (100 * 0.334)
                    + ((units - 300) * 0.516);
        } else {
            total = (200 * 0.218) + (100 * 0.334)
                    + (300 * 0.516)
                    + ((units - 600) * 0.546);
        }

        return total;
    }
}
