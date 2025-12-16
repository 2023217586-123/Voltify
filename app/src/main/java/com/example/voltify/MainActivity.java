package com.example.voltify;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * MainActivity for Voltify
 * - Uses Material-themed layout (activity_main.xml)
 * - Adds bills to SQLite via DataHelper
 * - Displays saved bills in a custom two-line list item
 * - Uses DB row id for robust view/delete actions
 */
public class MainActivity extends AppCompatActivity {

    Spinner spinnerMonth;
    EditText editUnits, editRebate;
    Button buttonCalc, buttonAbout;
    TextView textTotal, textFinal;
    ListView listViewResults;
    DataHelper dbHelper;

    String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ensure this matches provided layout

        // find views
        spinnerMonth = findViewById(R.id.spinnerMonth);
        editUnits = findViewById(R.id.editUnits);
        editRebate = findViewById(R.id.editRebate);
        buttonCalc = findViewById(R.id.buttonCalc);
        buttonAbout = findViewById(R.id.buttonAbout);
        textTotal = findViewById(R.id.textTotal);
        textFinal = findViewById(R.id.textFinal);
        listViewResults = findViewById(R.id.listViewResults);

        // spinner adapter
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(spAdapter);

        // DB helper
        dbHelper = new DataHelper(this);

        // initial list refresh
        RefreshList();

        // Calculate & Save
        buttonCalc.setOnClickListener(v -> {
            String month = spinnerMonth.getSelectedItem().toString();
            String unitsStr = editUnits.getText().toString().trim();
            String rebateStr = editRebate.getText().toString().trim();
            if (unitsStr.isEmpty()) {
                Toast.makeText(this, "Please enter units (kWh)", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rebateStr.isEmpty()) rebateStr = "0";

            int units;
            double rebate;
            try {
                units = Integer.parseInt(unitsStr);
                rebate = Double.parseDouble(rebateStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number entered", Toast.LENGTH_SHORT).show();
                return;
            }
            if (units < 0) {
                Toast.makeText(this, "Units cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rebate < 0 || rebate > 5) {
                Toast.makeText(this, "Rebate must be between 0% and 5%", Toast.LENGTH_SHORT).show();
                return;
            }

            double total = computeTotalCharges(units);
            double finalCost = applyRebate(total, rebate);

            textTotal.setText(String.format(Locale.US, "Total charges: RM %.2f", total));
            textFinal.setText(String.format(Locale.US, "Final cost (after rebate): RM %.2f", finalCost));

            // insert into DB
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("month", month);
            cv.put("units", units);
            cv.put("total", total);
            cv.put("rebate", rebate);
            cv.put("final", finalCost);
            long newId = db.insert("bill", null, cv);
            if (newId == -1) {
                Toast.makeText(this, "Failed to save record", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            }

            RefreshList();
            clearInputs();
        });

        // About button
        buttonAbout.setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
        });

        // Item click handler set inside RefreshList (after we populate ids), but provide a safe fallback here
        listViewResults.setOnItemClickListener((parent, view, position, id) -> {
            Object tag = listViewResults.getTag();
            if (tag instanceof int[]) {
                int[] ids = (int[]) tag;
                if (position >= 0 && position < ids.length) {
                    final int recordId = ids[position];
                    showItemOptions(recordId);
                }
            }
        });
    }

    private void clearInputs() {
        editUnits.setText("");
        editRebate.setText("");
    }

    // show a simple dialog with View / Delete for a record id
    private void showItemOptions(int recordId) {
        final CharSequence[] dialogitem = {"View Details", "Delete Record"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select Action");
        builder.setItems(dialogitem, (dialog, which) -> {
            if (which == 0) {
                Intent i = new Intent(MainActivity.this, ViewBillActivity.class);
                i.putExtra("id", recordId); // pass id to the view activity
                startActivity(i);
            } else {
                // delete by id
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                int rows = db.delete("bill", "id = ?", new String[]{ String.valueOf(recordId) });
                if (rows > 0) {
                    Toast.makeText(MainActivity.this, "Record removed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
                RefreshList();
            }
        });
        builder.create().show();
    }

    // compute using block rates (assignment rates in RM/kWh)
    public double computeTotalCharges(int units) {
        double total = 0.0;
        int remaining = units;
        int use;

        use = Math.min(remaining, 200);
        total += use * 0.218; remaining -= use;
        if (remaining <= 0) return total;

        use = Math.min(remaining, 100);
        total += use * 0.334; remaining -= use;
        if (remaining <= 0) return total;

        use = Math.min(remaining, 300);
        total += use * 0.516; remaining -= use;
        if (remaining <= 0) return total;

        total += remaining * 0.546;
        return total;
    }

    public double applyRebate(double total, double rebatePercent) {
        return total - (total * (rebatePercent / 100.0));
    }

    /**
     * RefreshList: queries DB and populates ListView using a custom adapter.
     * Also stores ids[] in the ListView tag so clicks can reference record ids.
     */
    public void RefreshList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT id, month, final FROM bill ORDER BY id DESC", null);
            final int count = (cursor == null) ? 0 : cursor.getCount();

            final int[] ids = new int[count];
            final String[] monthsList = new String[count];
            final String[] finalsList = new String[count];

            if (cursor != null && cursor.moveToFirst()) {
                int idx = 0;
                do {
                    ids[idx] = cursor.getInt(0);
                    monthsList[idx] = cursor.getString(1);
                    finalsList[idx] = String.format(Locale.US, "RM %.2f", cursor.getDouble(2));
                    idx++;
                } while (cursor.moveToNext());
            }

            // custom adapter: inflate list_item_bill for each row
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_bill, R.id.itemMonth, monthsList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View row = convertView;
                    if (row == null) {
                        row = getLayoutInflater().inflate(R.layout.list_item_bill, parent, false);
                    }
                    TextView tvMonth = row.findViewById(R.id.itemMonth);
                    TextView tvFinal = row.findViewById(R.id.itemFinal);
                    tvMonth.setText(monthsList[position]);
                    tvFinal.setText(finalsList[position]);
                    return row;
                }
            };

            listViewResults.setAdapter(adapter);
            // store ids for lookup on click
            listViewResults.setTag(ids);

            // set OnItemClickListener here to ensure correct tag reference (overrides fallback in onCreate)
            listViewResults.setOnItemClickListener((parent, view, position, id) -> {
                int[] tagIds = (int[]) listViewResults.getTag();
                if (tagIds != null && position >= 0 && position < tagIds.length) {
                    int recordId = tagIds[position];
                    showItemOptions(recordId);
                }
            });
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
