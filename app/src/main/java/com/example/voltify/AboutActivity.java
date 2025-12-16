package com.example.voltify;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textAbout = findViewById(R.id.textAbout);
        Button buttonBack = findViewById(R.id.buttonBack);

        textAbout.setText(
                "Student Name: Najidah Safiyyah Binti Ahmad Saifuzzaman\n" +
                        "Student ID: 2023217586\n" +
                        "Course: ICT602 Mobile Technology and Development\n\n" +
                        "© 2025 Voltify. All rights reserved."
        );

        buttonBack.setOnClickListener(v -> finish());
    }
}



