package com.example.quanlydeadline;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtGreeting;
    private FloatingActionButton fabAddDeadline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        txtGreeting = findViewById(R.id.txtGreeting);
        fabAddDeadline = findViewById(R.id.fabAddDeadline);

        String fullName =
                getIntent().getStringExtra("FULL_NAME");

        if(fullName != null){
            txtGreeting.setText(
                    "Xin chào, " + fullName + " 👋");
        }

        fabAddDeadline.setOnClickListener(v -> {
            
        });
    }
}