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

        // Navigate to saved bills
        buttonViewBills.setOnClickListener(v ->
                startActivity(new Intent(this, ListBillActivity.class))
        );

        // Navigate to about page
        buttonAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class))
        );
    }

    private void calculateAndSave() {

        String month = spinnerMonth.getSelectedItem().toString();
        String unitsStr = editUnits.getText().toString().trim();
        String rebateStr = editRebate.getText().toString().trim();

        // Clear previous errors
        editUnits.setError(null);
        editRebate.setError(null);

        // Case 1: Nothing entered
        if (unitsStr.isEmpty() && rebateStr.isEmpty()) {
            editUnits.setError("Please enter electricity usage");
            editRebate.setError("Please enter rebate (0 if none)");
            return;
        }

        // Case 2: Units missing
        if (unitsStr.isEmpty()) {
            editUnits.setError("Please enter electricity usage");
            return;
        }

        // Case 3: Rebate missing
        if (rebateStr.isEmpty()) {
            editRebate.setError("Please enter rebate (0 if none)");
            return;
        }

        // Validate units
        int units;
        try {
            units = Integer.parseInt(unitsStr);
            if (units <= 0) {
                editUnits.setError("Usage must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            editUnits.setError("Invalid number");
            return;
        }

        // Validate rebate
        double rebate;
        try {
            rebate = Double.parseDouble(rebateStr);
            if (rebate < 0 || rebate > 5) {
                editRebate.setError("Rebate must be between 0 and 5%");
                return;
            }
        } catch (NumberFormatException e) {
            editRebate.setError("Invalid rebate value");
            return;
        }

        // Calculate charges
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

        // Clear input fields
        editUnits.setText("");
        editRebate.setText("");
    }

    // Electricity tariff calculation
    private double calculateTotal(int units) {

        double total;

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
