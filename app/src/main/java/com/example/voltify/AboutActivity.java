package com.example.voltify;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textAbout = findViewById(R.id.textAbout);
        TextView textGithub = findViewById(R.id.textGithub);
        Button buttonBack = findViewById(R.id.buttonBack);


        textAbout.setText(
                "Student Name: Najidah Safiyyah Binti Ahmad Saifuzzaman\n" +
                        "Student ID: 2023217586\n" +
                        "Course: ICT602 Mobile Technology and Development\n\n" +
                        "© 2025 Voltify. Developed by Najifdah Safiyyah Binti Ahmad Saifuzzaman. All rights reserved."
        );

        textGithub.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/2023217586-123/Voltify"));
            startActivity(intent);
        });


        buttonBack.setOnClickListener(v -> finish());
    }
}



