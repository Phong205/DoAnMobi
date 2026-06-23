package com.example.quanlydeadline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlydeadline.controllers.ProjectListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        txtGreeting = findViewById(R.id.txtGreeting);

        String fullName = getIntent().getStringExtra("FULL_NAME");
        if (fullName != null) {
            txtGreeting.setText("Xin chào, " + fullName + " 👋");
        }

        // "Xem tất cả" → ProjectListActivity
        findViewById(R.id.tvSeeAll).setOnClickListener(v ->
                navigateToProjects()
        );

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Đang ở Home, không làm gì
                return true;
            } else if (id == R.id.nav_projects) {
                navigateToProjects();
                return true;
            } else if (id == R.id.nav_stats) {
                // TODO: mở StatsActivity khi có
                return true;
            } else if (id == R.id.nav_profile) {
                // TODO: mở ProfileActivity khi có
                return true;
            }
            return false;
        });
    }

    private void navigateToProjects() {
        Intent intent = new Intent(this, ProjectListActivity.class);
        startActivity(intent);
    }
}